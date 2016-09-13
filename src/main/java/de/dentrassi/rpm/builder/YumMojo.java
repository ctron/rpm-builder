/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Red Hat Inc - upgrade to package drone 0.14.0
 *******************************************************************************/
package de.dentrassi.rpm.builder;

import static java.util.Collections.singletonMap;
import static org.eclipse.packagedrone.utils.rpm.yum.ChecksumType.SHA256;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.eclipse.packagedrone.utils.io.FileSystemSpoolOutTarget;
import org.eclipse.packagedrone.utils.rpm.HashAlgorithm;
import org.eclipse.packagedrone.utils.rpm.info.RpmInformation;
import org.eclipse.packagedrone.utils.rpm.info.RpmInformations;
import org.eclipse.packagedrone.utils.rpm.parse.RpmInputStream;
import org.eclipse.packagedrone.utils.rpm.yum.RepositoryCreator;
import org.eclipse.packagedrone.utils.rpm.yum.RepositoryCreator.Builder;
import org.eclipse.packagedrone.utils.rpm.yum.RepositoryCreator.FileInformation;
import org.eclipse.packagedrone.utils.security.pgp.SigningStream;

/**
 * Build a YUM repository
 *
 * @author ctron
 */
@Mojo ( name = "yum-repository",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresProject = true,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresDependencyCollection = ResolutionScope.COMPILE,
        threadSafe = false )
public class YumMojo extends AbstractMojo
{
    /**
     * The maven project
     */
    @Parameter ( property = "project", readonly = true, required = true )
    protected MavenProject project;

    @Parameter ( defaultValue = "${session}", readonly = true, required = true )
    private MavenSession session;

    @Component
    private MavenProjectHelper projectHelper;

    /**
     * The location of the output directory.
     * <p>
     * This directory will be created if it does not exists.
     * </p>
     */
    @Parameter ( property = "yum.repository.output", defaultValue = "${project.build.directory}/yum" )
    private File outputDirectory;

    /**
     * A list of files to be added
     * <p>
     * All listed files will be added to the final repository.
     * </p>
     */
    @Parameter
    private List<File> files;

    /**
     * A list of directories
     * <p>
     * These directories will be scanned recursively for files with the
     * extension <code>.rpm</code>. All matching files will be added to the
     * final repository.
     * </p>
     */
    @Parameter
    private List<File> directories;

    private File packagesPath;

    /**
     * An optional signature descriptor for GPP signing the final YUM repository
     * <p>
     * Also see <a href="signing.html">signing</a>
     * </p>
     */
    @Parameter ( property = "rpm.signature" )
    private Signature signature;

    /**
     * Disable all repository signing
     */
    @Parameter ( property = "rpm.skipSigning", defaultValue = "false" )
    private boolean skipSigning = false;

    private Logger logger;

    @Override
    public void execute () throws MojoExecutionException, MojoFailureException
    {
        this.logger = new Logger ( getLog () );

        try
        {
            final Builder builder = new RepositoryCreator.Builder ();
            builder.setTarget ( new FileSystemSpoolOutTarget ( this.outputDirectory.toPath () ) );

            if ( !this.skipSigning )
            {
                final PGPPrivateKey privateKey = SigningHelper.loadKey ( this.signature, this.logger );
                if ( privateKey != null )
                {
                    final int digestAlgorithm = HashAlgorithm.from ( this.signature.getHashAlgorithm () ).getValue ();
                    builder.setSigning ( output -> new SigningStream ( output, privateKey, digestAlgorithm, false, "RPM builder Mojo - de.dentrassi.maven:rpm" ) );
                }
            }

            final RepositoryCreator creator = builder.build ();

            this.packagesPath = new File ( this.outputDirectory, "packages" );
            Files.createDirectories ( this.packagesPath.toPath () );

            int count = 0;

            final Set<Artifact> deps = this.project.getArtifacts ();
            if ( deps != null )
            {
                deps.forEach ( art -> addPackage ( creator, art.getFile () ) );
                count++;
            }
            if ( this.files != null )
            {
                this.files.forEach ( file -> addPackage ( creator, file ) );
                count++;
            }
            if ( this.directories != null )
            {
                for ( final File dir : this.directories )
                {
                    Files.walkFileTree ( dir.toPath (), new SimpleFileVisitor<Path> () {
                        @Override
                        public FileVisitResult visitFile ( final Path file, final BasicFileAttributes attrs ) throws IOException
                        {
                            if ( file.getFileName ().toString ().toLowerCase ().endsWith ( ".rpm" ) )
                            {
                                addPackage ( creator, file );
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    } );
                }
            }

            getLog ().info ( String.format ( "Added %s packages to the repository", count ) );
        }
        catch ( final IOException e )
        {
            throw new MojoExecutionException ( "Failed to write repository", e );
        }
    }

    private void addPackage ( final RepositoryCreator creator, final File file )
    {
        addPackage ( creator, file.toPath () );
    }

    private void addPackage ( final RepositoryCreator creator, final Path path )
    {
        try
        {
            creator.process ( context -> {
                final String checksum = makeChecksum ( path );
                final String fileName = path.getFileName ().toString ();
                final String location = "packages/" + fileName;
                final FileInformation fileInformation = new FileInformation ( Files.getLastModifiedTime ( path ).toInstant (), Files.size ( path ), location );

                final RpmInformation rpmInformation;
                try ( RpmInputStream ris = new RpmInputStream ( Files.newInputStream ( path ) ) )
                {
                    rpmInformation = RpmInformations.makeInformation ( ris );
                }

                context.addPackage ( fileInformation, rpmInformation, singletonMap ( SHA256, checksum ), SHA256 );

                Files.copy ( path, this.packagesPath.toPath ().resolve ( fileName ), StandardCopyOption.COPY_ATTRIBUTES );
            } );
        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( e );
        }
    }

    private String makeChecksum ( final Path path ) throws IOException
    {
        try ( InputStream is = Files.newInputStream ( path ) )
        {
            return DigestUtils.sha256Hex ( is ).toLowerCase ();
        }
    }
}
