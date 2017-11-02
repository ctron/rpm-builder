/*******************************************************************************
 * Copyright (c) 2016, 2017 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Red Hat Inc - upgrade to package drone 0.14.0
 *     Bernd Warmuth - bugfix target folder creation
 *     Oliver Richter - Made packageName & defaultScriptInterpreter configurable
 *******************************************************************************/
package de.dentrassi.rpm.builder;

import static com.google.common.io.Files.readFirstLine;
import static java.nio.file.Files.walkFileTree;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.maven.model.License;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.eclipse.packagedrone.utils.rpm.HashAlgorithm;
import org.eclipse.packagedrone.utils.rpm.RpmVersion;
import org.eclipse.packagedrone.utils.rpm.build.BuilderContext;
import org.eclipse.packagedrone.utils.rpm.build.RpmBuilder;
import org.eclipse.packagedrone.utils.rpm.build.RpmBuilder.PackageInformation;
import org.eclipse.packagedrone.utils.rpm.deps.RpmDependencyFlags;
import org.eclipse.packagedrone.utils.rpm.signature.RsaHeaderSignatureProcessor;
import org.eclipse.packagedrone.utils.rpm.signature.SignatureProcessor;

import com.google.common.base.Strings;
import com.google.common.io.CharSource;

import de.dentrassi.rpm.builder.Naming.Case;
import de.dentrassi.rpm.builder.PackageEntry.Collector;

/**
 * Build an RPM file
 *
 * @author ctron
 */
@Mojo ( name = "rpm", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = false )
public class RpmMojo extends AbstractMojo
{
    private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

    /**
     * The maven project
     */
    @Parameter ( property = "project", readonly = true, required = true )
    protected MavenProject project;

    @Component
    private MavenProjectHelper projectHelper;

    /**
     * The version string to process
     */
    @Parameter ( defaultValue = "${project.version}" )
    private String version;

    /**
     * The RPM package name
     */
    @Parameter ( defaultValue = "${project.artifactId}", property = "rpm.packageName" )
    private String packageName;

    /**
     * The architecture
     */
    @Parameter ( defaultValue = "noarch", property = "rpm.architecture" )
    private String architecture = "noarch";

    /**
     * The prefix of the release if this is a snapshot build, will be suffixed
     * with the snapshot build id
     * <p>
     * Also see: {@link #snapshotBuildId}
     * </p>
     */
    @Parameter ( defaultValue = "0.", property = "rpm.snapshotReleasePrefix" )
    private String snapshotReleasePrefix = "0.";

    /**
     * Set the build id which is used when a snapshot build is active.
     * <p>
     * If this parameter is left unset or empty then the current time (UTC) in
     * the format {@code yyyyMMddHHmm} will be used.
     * </p>
     *
     * @since 0.6.0
     */
    @Parameter ( property = "rpm.snapshotBuildId", required = false )
    private String snapshotBuildId;

    /**
     * The release which will be used if this is not a snapshot build
     */
    @Parameter ( property = "rpm.release", defaultValue = "1" )
    private String release = "1";

    /**
     * Always use the "release" string
     * <p>
     * This parameter enforces the build to always use the RPM release
     * information from the parameter "release", whether this is a snapshot
     * build or not
     * </p>
     *
     * @since 0.6.0
     */
    @Parameter ( property = "rpm.forceRelease", defaultValue = "false" )
    private boolean forceRelease = false;

    /**
     * The classifier of the attached rpm
     */
    @Parameter ( property = "rpm.classifier", defaultValue = "rpm" )
    private String classifier = "rpm";

    /**
     * Whether to attach the output file
     */
    @Parameter ( property = "rpm.attach", defaultValue = "true" )
    private boolean attach = true;

    /**
     * The RPM epoch, leave unset for default
     */
    @Parameter ( property = "rpm.epoch" )
    private Integer epoch;

    /**
     * The "summary" field of the RPM file
     * <p>
     * This defaults to the project name
     * </p>
     */
    @Parameter ( property = "rpm.summary", defaultValue = "${project.name}" )
    private String summary;

    /**
     * The "description" field of the RPM file
     * <p>
     * This defaults to the Maven project description
     * </p>
     */
    @Parameter ( property = "rpm.description", defaultValue = "${project.description}" )
    private String description;

    /**
     * The RPM group
     * <p>
     * See also
     * <a href="https://fedoraproject.org/wiki/RPMGroups">https://fedoraproject.
     * org/wiki/RPMGroups</a>
     * </p>
     */
    @Parameter ( property = "rpm.group", defaultValue = "Unspecified" )
    private String group;

    /**
     * The "distribution" field in the RPM file
     */
    @Parameter ( property = "rpm.distribution" )
    private String distribution;

    /**
     * Whether the plugin should try to evaluate to hostname
     * <p>
     * If set to {@code false}, then he build hostname {@code localhost} will be
     * used instead of the actual hostname
     * </p>
     */
    @Parameter ( property = "rpm.evalHostname", defaultValue = "true" )
    private boolean evalHostname = true;

    /**
     * The license of the RPM file
     * <p>
     * This defaults to a comma separated list of all license names specified in
     * the projects POM file.
     * </p>
     */
    @Parameter ( property = "rpm.license" )
    private String license;

    /**
     * The vendor name of the RPM file
     * <p>
     * This defaults to the name of the organization in the POM file.
     * </p>
     */
    @Parameter ( property = "rpm.vendor" )
    private String vendor;

    /**
     * The name of the packager in the RPM file
     * <p>
     * This defaults to <tt>${project.organization.name}
     * &lt;${project.organization.url}&gt;</tt> if both values are set.
     * </p>
     * <p>
     * See also <a href=
     * "http://www.rpm.org/max-rpm/s1-rpm-inside-tags.html#S3-RPM-INSIDE-PACKAGER-TAG">
     * http://www.rpm.org/max-rpm/s1-rpm-inside-tags.html#S3-RPM-INSIDE-PACKAGER
     * -TAG</a>
     * </p>
     */
    @Parameter ( property = "rpm.packager" )
    private String packager;

    /**
     * The actual payload/file entries
     * <p>
     * Also see <a href="entry.html">entries</a>
     * </p>
     * <p>
     * This is a list of {@code <entry>} elements with additional information:
     * </p>
     *
     * <pre>
     * &lt;entries&gt;
     *   &lt;entry&gt;
     *     &lt;!-- target name --&gt;
     *     &lt;name&gt;/etc/foo/bar.conf&lt;/name&gt;
     *
     *     &lt;!-- either one of:
     *       &lt;file&gt;src/main/resources/bar.conf&lt;/file&gt;
     *       &lt;directory&gt;true&lt;/directory&gt;
     *       &lt;collect&gt;
     *         &lt;from&gt;src/main/resources/dir&lt;/from&gt;
     *       &lt;/collect&gt;
     *     -->
     *
     *   &lt;/entry&gt;
     * &lt;/entries&gt;
     * </pre>
     */
    @Parameter
    private List<PackageEntry> entries = new LinkedList<> ();

    /**
     * Rulesets to configure the file information like "user", "modes", etc.
     * <p>
     * Also see <a href="rulesets.html">rulesets</a>
     * </p>
     */
    @Parameter
    private List<Ruleset> rulesets = new LinkedList<> ();

    /**
     * The default ruleset to use if no other is specified
     */
    @Parameter
    private String defaultRuleset;

    private Logger logger;

    private RulesetEvaluator eval;

    /**
     * A script which is run before the installation takes place
     * <p>
     * Also see <a href="rulesets.html">scripts</a>
     * </p>
     */
    @Parameter
    private Script beforeInstallation;

    /**
     * A script which is run after the installation took place
     * <p>
     * Also see <a href="rulesets.html">scripts</a>
     * </p>
     */
    @Parameter
    private Script afterInstallation;

    /**
     * A script which is run before the removal takes place
     * <p>
     * Also see <a href="rulesets.html">scripts</a>
     * </p>
     */
    @Parameter
    private Script beforeRemoval;

    /**
     * A script which is run after the removal took place
     * <p>
     * Also see <a href="rulesets.html">scripts</a>
     * </p>
     */
    @Parameter
    private Script afterRemoval;

    /**
     * The default script interpreter which is used if neither the script has
     * one set explicitly, nor one could be detected
     */
    @Parameter ( property = "rpm.defaultScriptInterpreter", defaultValue = "/bin/sh" )
    private String defaultScriptInterpreter;

    /**
     * RPM package requirements
     * <p>
     * Also see <a href="deps.html">dependencies</a>
     * </p>
     */
    @Parameter
    private final List<Dependency> requires = new LinkedList<> ();

    /**
     * RPM provides information
     * <p>
     * Also see <a href="deps.html">dependencies</a>
     * </p>
     */
    @Parameter
    private final List<SimpleDependency> provides = new LinkedList<> ();

    /**
     * RPM package conflicts
     * <p>
     * Also see <a href="deps.html">dependencies</a>
     * </p>
     */
    @Parameter
    private final List<SimpleDependency> conflicts = new LinkedList<> ();

    /**
     * RPM obsoletes information
     * <p>
     * Also see <a href="deps.html">dependencies</a>
     * </p>
     */
    @Parameter
    private final List<SimpleDependency> obsoletes = new LinkedList<> ();

    /**
     * RPM package requirements needed before the installation starts
     * <p>
     * Also see <a href="deps.html">dependencies</a>
     * </p>
     */
    @Parameter
    private final List<SimpleDependency> prerequisites = new LinkedList<> ();

    /**
     * An optional signature descriptor for GPP signing the final RPM
     * <p>
     * Also see <a href="signing.html">signing</a>
     * </p>
     */
    @Parameter ( property = "rpm.signature" )
    private Signature signature;

    /**
     * Disable all package signing
     */
    @Parameter ( property = "rpm.skipSigning", defaultValue = "false" )
    private boolean skipSigning = false;

    /**
     * Provide package naming options
     * <p>
     * Also see <a href="naming.html">naming</a>
     * </p>
     */
    @Parameter ( property = "rpm.naming" )
    private Naming naming;

    public void setSkipSigning ( final boolean skipSigning )
    {
        this.skipSigning = skipSigning;
    }

    public void setNaming ( final Naming naming )
    {
        this.naming = naming;
    }

    @Override
    public void execute () throws MojoExecutionException, MojoFailureException
    {
        this.logger = new Logger ( getLog () );

        this.eval = new RulesetEvaluator ( this.rulesets );

        final Path targetDir = Paths.get ( this.project.getBuild ().getDirectory () );

        if ( !Files.exists ( targetDir ) )
        {
            try
            {
                Files.createDirectories ( targetDir );
            }
            catch ( final IOException ioe )
            {
                this.logger.debug ( "Unable to create target directory {}", targetDir );
                throw new MojoExecutionException ( "RPM build failed.", ioe );

            }
        }

        this.logger.info ( "Writing to target to: %s", targetDir );
        this.logger.debug ( "Default script interpreter: %s", this.defaultScriptInterpreter );
        this.logger.debug ( "Default ruleset: %s", this.defaultRuleset );

        final String packageName = makePackageName ();
        final RpmVersion version = makeVersion ();

        this.logger.info ( "RPM base information - name: %s, version: %s, arch: %s", packageName, version, this.architecture );

        try ( RpmBuilder builder = new RpmBuilder ( packageName, version, this.architecture, targetDir ) )
        {
            this.logger.info ( "Writing target file: %s", builder.getTargetFile () );

            fillPackageInformation ( builder );
            fillScripts ( builder );
            fillDependencies ( builder );
            fillPayload ( builder );

            // add signer

            if ( !this.skipSigning && this.signature != null )
            {
                final SignatureProcessor signer = makeRsaSigner ( this.signature );
                if ( signer != null )
                {
                    builder.addSignatureProcessor ( signer );
                }
            }

            // finally build the file

            builder.build ();

            if ( this.attach )
            {
                this.projectHelper.attachArtifact ( this.project, "rpm", this.classifier, builder.getTargetFile ().toFile () );
            }
        }
        catch ( final IOException e )
        {
            throw new MojoExecutionException ( "Failed to write RPM", e );
        }
    }

    private SignatureProcessor makeRsaSigner ( final Signature signature ) throws MojoExecutionException, MojoFailureException
    {
        final PGPPrivateKey privateKey = SigningHelper.loadKey ( signature, this.logger );
        if ( privateKey == null )
        {
            return null;
        }
        return new RsaHeaderSignatureProcessor ( privateKey, HashAlgorithm.from ( signature.getHashAlgorithm () ) );
    }

    @FunctionalInterface
    private interface DependencyAdder
    {
        public void add ( String name, String version, RpmDependencyFlags[] flags );
    }

    private void fillDependencies ( final RpmBuilder builder )
    {
        addAllDependencies ( "require", this.requires, builder::addRequirement, RpmMojo::validateName, null );
        addAllDependencies ( "prerequire", this.prerequisites, builder::addRequirement, RpmMojo::validateName, flags -> flags.add ( RpmDependencyFlags.PREREQ ) );
        addAllDependencies ( "provide", this.provides, builder::addProvides, ( (Consumer<SimpleDependency>)RpmMojo::validateName ).andThen ( this::validateNoVersion ), null );
        addAllDependencies ( "conflict", this.conflicts, builder::addConflicts, RpmMojo::validateName, null );
        addAllDependencies ( "obsolete", this.obsoletes, builder::addObsoletes, RpmMojo::validateName, null );
    }

    private static void validateName ( final SimpleDependency dep )
    {
        if ( Strings.isNullOrEmpty ( dep.getName () ) )
        {
            throw new IllegalStateException ( "'name' of dependency must be set" );
        }
    }

    private void validateNoVersion ( final SimpleDependency dep )
    {
        if ( !Strings.isNullOrEmpty ( dep.getVersion () ) )
        {
            getLog ().warn ( String.format ( "Provides should not have a version: %s : %s. Use at your own risk!", dep.getName (), dep.getVersion () ) );
        }
    }

    private <T extends SimpleDependency> void addAllDependencies ( final String depName, final List<T> deps, final DependencyAdder adder, final Consumer<T> validator, final Consumer<Set<RpmDependencyFlags>> flagsCustomizer )
    {
        if ( deps == null )
        {
            return;
        }

        for ( final T dep : deps )
        {
            validator.accept ( dep );

            final String name = dep.getName ();
            final String version = dep.getVersion ();
            final Set<RpmDependencyFlags> flags = dep.getFlags ();

            if ( flagsCustomizer != null )
            {
                flagsCustomizer.accept ( flags );
            }

            this.logger.info ( "Adding dependency [%s]: name = %s, version = %s, flags = %s", depName, name, version, flags );

            adder.add ( name, version, flags.toArray ( new RpmDependencyFlags[0] ) );
        }
    }

    private void fillScripts ( final RpmBuilder builder ) throws IOException
    {
        setScript ( "prein", this.beforeInstallation, builder::setPreInstallationScript );
        setScript ( "postin", this.afterInstallation, builder::setPostInstallationScript );
        setScript ( "prerm", this.beforeRemoval, builder::setPreRemoveScript );
        setScript ( "postrm", this.afterRemoval, builder::setPostRemoveScript );
    }

    private void setScript ( final String scriptName, final Script script, final ScriptSetter setter ) throws IOException
    {
        if ( script == null )
        {
            return;
        }

        final String scriptContent = script.makeScriptContent ();

        if ( Strings.isNullOrEmpty ( scriptContent ) )
        {
            return;
        }

        String interpreter = script.getInterpreter ();
        this.logger.debug ( "[script %s:]: explicit interpreter: %s", scriptName, interpreter );

        if ( Strings.isNullOrEmpty ( interpreter ) )
        {
            interpreter = detectInterpreter ( scriptContent );
            this.logger.debug ( "[script %s:]: detected interpreter: %s", scriptName, interpreter );
        }
        if ( Strings.isNullOrEmpty ( interpreter ) )
        {
            interpreter = this.defaultScriptInterpreter;
            this.logger.debug ( "[script %s:]: default interpreter: %s", scriptName, interpreter );
        }
        this.logger.info ( "[script %s]: Using script interpreter: %s", scriptName, interpreter );
        this.logger.debug ( "[script %s]: %s", scriptName, scriptContent );

        setter.accept ( interpreter, scriptContent );
    }

    private String detectInterpreter ( final String scriptContent ) throws IOException
    {
        final String firstLine = CharSource.wrap ( scriptContent ).readFirstLine ();
        if ( Strings.isNullOrEmpty ( firstLine ) )
        {
            return null;
        }

        if ( firstLine.startsWith ( "#!" ) && firstLine.length () > 2 )
        {
            return firstLine.substring ( 2 );
        }

        return null;
    }

    protected void fillPayload ( final RpmBuilder builder ) throws MojoFailureException, IOException
    {
        if ( this.entries == null )
        {
            return;
        }

        final BuilderContext ctx = builder.newContext ();

        this.logger.debug ( "Building payload:" );

        for ( final PackageEntry entry : this.entries )
        {
            try
            {
                entry.validate ();
            }
            catch ( final IllegalStateException e )
            {
                throw new MojoFailureException ( e.getMessage () );
            }

            fillFromEntry ( ctx, entry );
        }
    }

    private void fillFromEntry ( final BuilderContext ctx, final PackageEntry entry ) throws IOException
    {
        this.logger.debug ( "  %s:", entry.getName () );

        if ( entry.getDirectory () != null && entry.getDirectory () )
        {
            this.logger.debug ( "    as directory:" );
            ctx.addDirectory ( entry.getName (), makeProvider ( entry, "      - " ) );
        }
        else if ( entry.getFile () != null )
        {
            this.logger.debug ( "    as file:" );
            final Path source = entry.getFile ().toPath ().toAbsolutePath ();
            this.logger.debug ( "      - source: %s", source );

            ctx.addFile ( entry.getName (), source, makeProvider ( entry, "      - " ) );
        }
        else if ( entry.getLinkTo () != null )
        {
            this.logger.debug ( "    as symbolic link:" );
            this.logger.debug ( "      - linkTo: %s", entry.getLinkTo () );
            ctx.addSymbolicLink ( entry.getName (), entry.getLinkTo (), makeProvider ( entry, "      - " ) );
        }
        else if ( entry.getCollect () != null )
        {
            this.logger.debug ( "    as collector:" );

            final Collector collector = entry.getCollect ();

            this.logger.debug ( "      - configuration: %s", collector );

            final String padding = "          ";

            final Path from = collector.getFrom ().toPath ();
            final String targetPrefix = entry.getName ().endsWith ( "/" ) ? entry.getName () : entry.getName () + "/";

            this.logger.debug ( "      - files:" );

            final MojoFileInformationProvider provider = makeProvider ( entry, "            - " );

            walkFileTree ( from, new SimpleFileVisitor<Path> () {
                @Override
                public FileVisitResult visitFile ( final Path file, final BasicFileAttributes attrs ) throws IOException
                {
                    final Path relative = from.relativize ( file );
                    final String targetName = makeUnix ( targetPrefix + relative.toString () );

                    if ( java.nio.file.Files.isSymbolicLink ( file ) )
                    {
                        RpmMojo.this.logger.debug ( "%s%s (symlink)", padding, file );
                        if ( collector.isSymbolicLinks () )
                        {
                            final Path sym = java.nio.file.Files.readSymbolicLink ( file );
                            RpmMojo.this.logger.debug ( "%s%s (symlink)", padding, file );
                            RpmMojo.this.logger.debug ( "%s  - target: %s", padding, targetName );
                            RpmMojo.this.logger.debug ( "%s  - linkTo: %s", padding, sym.toString () );
                        }
                        else
                        {
                            RpmMojo.this.logger.debug ( "%s%s (symlink) - ignoring symbolic links", padding, file );
                        }
                    }
                    else
                    {
                        RpmMojo.this.logger.debug ( "%s%s (file)", padding, file );
                        RpmMojo.this.logger.debug ( "%s  - target: %s", padding, targetName );

                        ctx.addFile ( targetName, file, provider );
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory ( final Path dir, final BasicFileAttributes attrs ) throws IOException
                {
                    // only add when we add directories and it is not be base
                    if ( collector.isDirectories () && !dir.equals ( from ) )
                    {
                        RpmMojo.this.logger.debug ( "%s%s (dir)", padding, dir );
                        final Path relative = from.relativize ( dir );
                        final String targetName = makeUnix ( targetPrefix + relative.toString () );
                        RpmMojo.this.logger.debug ( "%s  - target: %s", padding, targetName );
                        ctx.addDirectory ( targetName, provider );
                    }
                    return FileVisitResult.CONTINUE;
                }
            } );
        }
    }

    protected String makeUnix ( final String path )
    {
        return path.replace ( "\\", "/" );
    }

    private MojoFileInformationProvider makeProvider ( final PackageEntry entry, final String padding )
    {
        String ruleset = this.defaultRuleset;
        if ( entry.getRuleset () != null && !entry.getRuleset ().isEmpty () )
        {
            this.logger.debug ( "Using specified ruleset: '%s'", entry.getRuleset () );
            ruleset = entry.getRuleset ();
        }
        else if ( this.defaultRuleset != null && !this.defaultRuleset.isEmpty () )
        {
            this.logger.debug ( "Using default ruleset: '%s'", this.defaultRuleset );
        }
        return new MojoFileInformationProvider ( this.eval, ruleset, entry, l -> this.logger.debug ( "%s%s", padding, l ) );
    }

    private String makePackageName ()
    {
        final Case nameCase;
        if ( this.naming == null )
        {
            nameCase = Case.UNMODIFIED;
            if ( !this.packageName.toLowerCase ().equals ( this.packageName ) )
            {
                getLog ().warn ( "Since version 0.9.0 of the RPM builder mojo the default behavior of forcing a lower case package name was removed. This package name seems to contain non-lowercase characters. It is possible to restore the previous behavior by setting the 'case' value in the 'naming' element." );
            }
        }
        else
        {
            nameCase = this.naming.getCase ();
        }

        switch ( nameCase )
        {
            case LOWERCASE:
                return this.packageName.trim ().toLowerCase ();
            case UNMODIFIED:
            default:
                return this.packageName.trim ();
        }
    }

    private RpmVersion makeVersion ()
    {
        if ( !this.forceRelease && isSnapshotVersion () )
        {
            this.logger.info ( "Building with SNAPSHOT version" );
            final String baseVersion = this.project.getVersion ().substring ( 0, this.project.getVersion ().length () - SNAPSHOT_SUFFIX.length () );
            return new RpmVersion ( this.epoch, baseVersion, makeSnapshotReleaseString () );
        }
        return new RpmVersion ( this.epoch, this.version, this.release );
    }

    private boolean isSnapshotVersion ()
    {
        return this.project.getVersion ().endsWith ( SNAPSHOT_SUFFIX );
    }

    private String makeSnapshotReleaseString ()
    {
        if ( this.snapshotBuildId == null || this.snapshotBuildId.isEmpty () )
        {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern ( "yyyyMMddHHmm", Locale.ROOT );
            return this.snapshotReleasePrefix + formatter.format ( Instant.now ().atOffset ( ZoneOffset.UTC ) );
        }
        else
        {
            return this.snapshotReleasePrefix + this.snapshotBuildId;
        }
    }

    protected void fillPackageInformation ( final RpmBuilder builder )
    {
        final PackageInformation pinfo = builder.getInformation ();

        ifSet ( pinfo::setDescription, this.description );
        ifSet ( pinfo::setSummary, this.summary );
        ifSet ( pinfo::setGroup, this.group );
        ifSet ( pinfo::setDistribution, this.distribution );

        if ( this.evalHostname )
        {
            ifSet ( pinfo::setBuildHost, makeHostname () );
        }

        ifSet ( pinfo::setUrl, this.project.getUrl () );
        ifSet ( pinfo::setVendor, this.vendor, this::makeVendor );
        ifSet ( pinfo::setPackager, this.packager, this::makePackager );

        ifSet ( pinfo::setLicense, this.license, this::makeLicense );
    }

    private String makeVendor ()
    {
        if ( this.project.getOrganization () != null )
        {
            return this.project.getOrganization ().getName ();
        }
        return null;
    }

    private String makePackager ()
    {
        if ( this.project.getOrganization () == null )
        {
            return null;
        }

        final String org = this.project.getOrganization ().getName ();
        final String url = this.project.getOrganization ().getUrl ();

        if ( org == null || url == null || org.isEmpty () || url.isEmpty () )
        {
            return null;
        }

        return String.format ( "%s <%s>", org, url );
    }

    private String makeLicense ()
    {
        return this.project.getLicenses ().stream ().map ( License::getName ).collect ( Collectors.joining ( ", " ) );
    }

    private String makeHostname ()
    {
        String hostname;

        try
        {
            hostname = readFirstLine ( new File ( "/etc/hostname" ), StandardCharsets.US_ASCII );

            if ( hostname != null && !hostname.isEmpty () )
            {
                this.logger.debug ( "Hostname: from /etc/hostname -> '%s'", hostname );
                return hostname;
            }
        }
        catch ( final IOException e )
        {
        }

        hostname = System.getenv ( "COMPUTERNAME" );
        if ( hostname != null && !hostname.isEmpty () )
        {
            this.logger.debug ( "Hostname: from COMPUTERNAME -> '%s'", hostname );
            return hostname.toLowerCase ();
        }

        hostname = System.getenv ( "hostname" );
        if ( hostname != null && !hostname.isEmpty () )
        {
            this.logger.debug ( "Hostname: from hostname -> '%s'", hostname );
            return hostname;
        }

        try
        {
            hostname = InetAddress.getLocalHost ().getHostName ();
            this.logger.debug ( "Hostname: from lookup -> '%s'", hostname );
            return hostname;
        }
        catch ( final UnknownHostException e )
        {
            this.logger.debug ( "Hostname: Falling back to 'localhost'" );
            return "localhost";
        }
    }

    private static interface StringSupplier extends Supplier<String>
    {
    }

    private static void ifSet ( final Consumer<String> setter, final String value, final StringSupplier... suppliers )
    {
        if ( value != null && !value.isEmpty () )
        {
            setter.accept ( value );
            return;
        }

        for ( final StringSupplier sup : suppliers )
        {
            final String v = sup.get ();
            if ( v != null && !v.isEmpty () )
            {
                setter.accept ( v );
                return;
            }
        }
    }

}
