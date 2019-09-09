/*******************************************************************************
 * Copyright (c) 2016, 2017 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Red Hat Inc - upgrade to package drone 0.14.0
 *     Bernd Warmuth - introduced skipDependencies property,
                       only consider dependencies of type "rpm",
                       fixed repodata creation for multiple rpm packages
 *******************************************************************************/
package de.dentrassi.rpm.builder;

import static java.util.Collections.singletonMap;
import static org.eclipse.packager.rpm.HashAlgorithm.SHA256;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.eclipse.packager.io.FileSystemSpoolOutTarget;
import org.eclipse.packager.rpm.HashAlgorithm;
import org.eclipse.packager.rpm.info.RpmInformation;
import org.eclipse.packager.rpm.info.RpmInformations;
import org.eclipse.packager.rpm.parse.RpmInputStream;
import org.eclipse.packager.rpm.yum.RepositoryCreator;
import org.eclipse.packager.rpm.yum.RepositoryCreator.Builder;
import org.eclipse.packager.rpm.yum.RepositoryCreator.Context;
import org.eclipse.packager.rpm.yum.RepositoryCreator.FileInformation;
import org.eclipse.packager.security.pgp.SigningStream;

import com.google.common.collect.Lists;

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

    /**
     * Disable the use of RPMs from maven dependency artifacts
     */
    @Parameter ( property = "rpm.skipDependencies", defaultValue = "false" )
    private boolean skipDependencies = false;

    /**
     * Disable the mojo altogether.
     *
     * @since 1.1.1
     */
    @Parameter ( property = "yum.skip", defaultValue = "false" )
    private boolean skip;

    public void setSkip ( final boolean skip )
    {
        this.skip = skip;
    }

    private Logger logger;

    @Override
    public void execute () throws MojoExecutionException, MojoFailureException
    {
        this.logger = new Logger ( getLog () );

        if ( this.skip )
        {
            this.logger.debug ( "Skipping execution" );
            return;
        }

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

            final Collection<Path> paths = Lists.newArrayList ();

            if ( !this.skipDependencies )
            {
                final Set<Artifact> deps = this.project.getArtifacts ();
                if ( deps != null )
                {
                    paths.addAll ( deps.stream ()//
                            .filter ( d -> d.getType ().equalsIgnoreCase ( "rpm" ) )//
                            .map ( d -> d.getFile ().toPath () )//
                            .collect ( Collectors.toList () ) );
                }
            }
            else
            {
                this.logger.debug ( "Skipped RPM artifacts from maven dependencies" );
            }

            if ( this.files != null )
            {
                paths.addAll ( this.files.stream ().map ( f -> f.toPath () ).collect ( Collectors.toList () ) );
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
                                paths.add ( file );
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    } );
                }
            }

            addPackageList ( creator, paths );
        }
        catch ( final IOException e )
        {
            throw new MojoExecutionException ( "Failed to write repository", e );
        }
    }

    private void addPackageList ( final RepositoryCreator creator, final Collection<Path> paths )
    {
        try
        {
            creator.process ( context -> {
                for ( final Path p : paths )
                {
                    addSinglePackage ( p, context );
                }
                getLog ().info ( String.format ( "Added %s packages to the repository", paths.size () ) );
            } );
        }
        catch ( final IOException e )
        {
            throw new RuntimeException ( e );
        }
    }

    private void addSinglePackage ( final Path path, final Context context ) throws IOException
    {
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
    }

    private String makeChecksum ( final Path path ) throws IOException
    {
        try ( InputStream is = Files.newInputStream ( path ) )
        {
            return DigestUtils.sha256Hex ( is ).toLowerCase ();
        }
    }
}
