/*******************************************************************************
 * Copyright (c) 2021, 2022 dranuhl@users.noreply.github.com and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0
 *******************************************************************************/
package de.dentrassi.rpm.builder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileOwnerAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.cpio.CpioArchiveInputStream;
import org.apache.commons.compress.archivers.cpio.CpioConstants;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.packager.rpm.RpmTag;
import org.eclipse.packager.rpm.parse.InputHeader;
import org.eclipse.packager.rpm.parse.RpmInputStream;

/**
 * Unpack a RPM file.
 *
 * <p><strong>Limitations / Restrictions</strong></p>
 * <ul>
 *   <li>Symbolic links are not supported, but details about them are written to debug log,</li>
 *   <li>only directories and regular files are supported currently, partially due to Java Nio limitations,</li>
 *   <li>
 *     POSIX file permissions can only be preserved if supported by file system.  Otherwise just read/write will
 *     be set,
 *   </li>
 *   <li>
 *     user ownership can only be preserved on POSIX and/or ACL file systems, as long as user exists and extracting
 *     user has suitable permissions (cmp. {@code #preserveOwner}),
 *   </li>
 *   <li>
 *     group ownership can only be preserved on POSIX file systems, as long as group exists and extracting user has
 *     suitable permissions (cmp. {@code #preserveOwner}),
 *   </li>
 *   <li>contained file names must be valid.</li>
 * </ul>
 *
 * <p><strong>File Name Validation</strong></p>
 * <p>File names are validated before unpacking and must satisfy the following conditions to be accepted.  Any file
 * name violating will result in abortion.</p>
 * <ul>
 *   <li>at most 256 characters in length,</li>
 *   <li>must not contain {@code "../"} sequence,</li>
 *   <li>
 *     printable characters excluding backslash, colon, wild card characters ({@code *} and {@code ?}) and
 *     shell special characters ({@code <}, {@code >} and {@code |}).
 *   </li>
 * </ul>
 *
 * @author dranuhl
 */
@Mojo(name = "unpack", requiresProject = false, defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = false)
public final class RpmUnpackMojo extends AbstractMojo {
    // buffer size used for reading and writing
    private static final int BUFFER_SIZE = 8192;

    /**
     * Maximum acceptable file name length.
     *
     * @see #makeTargetFile(Path, String)
     */
    private static final int MAX_FILENAME_LENGTH = 256;

    /**
     * Regular expression to validate character set used by file names.
     *
     * <p>Accepts printable characters excluding:</p>
     * <ul>
     * <li>backslash</li>
     * <li>colon</li>
     * <li>wild card characters ({@code *} and {@code ?})</li>
     * <li>usual shell special characters, like {@code <}, {@code >} and {@code |}</li>
     * </ul>
     *
     * @see #makeTargetFile(Path, String)
     */
    private static final Pattern FILENAME_PATTERN = Pattern.compile("^[\\p{Print}&&[^\\\\:*?\"<>|]]+$");

    private Logger logger;

    /**
     * RPM file to unpack.
     */
    @Parameter(property = "rpm.file", required = true)
    File rpmFile;

    /**
     * Directory to unpack to.
     */
    @Parameter(property = "rpm.unpackDirectory", defaultValue = "${project.build.directory}/rpm/unpack")
    File unpackDirectory;

    /**
     * Retain file modification times from the RPM when creating files.
     *
     * <p>If {@code true} (the default) it will be attempted to preserve the last-modification time of the files.
     * If {@code false} or the user or system does not support it, the time will reflect the time of unpacking.</p>
     */
    @Parameter(property = "rpm.preserveLastModificationTime", defaultValue = "true")
    boolean preserveLastModificationTime;

    /**
     * Specifies whether to change the ownership of the files.
     *
     * <p>If {@code false} (the default) they will be owned by the user extracting them.</p>
     *
     * <p>If {@code true} it will be attempted to change the ownership of the files. For this the underlying file system
     * must supports POSIX permissions and the extracting user have suitable OS permissions to do so.  The process will
     * not fail due to lack of support or permissions, the files will be just remain owned by the user extracting
     * them.</p>
     *
     * <p><strong>Warning:</strong> Use with caution.</p>
     */
    @Parameter(property = "rpm.preserveOwner", defaultValue = "false")
    boolean preserveOwner;

    public RpmUnpackMojo() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.logger = new Logger(getLog());

        final Path targetDir = this.unpackDirectory.toPath();

        if (!Files.exists(targetDir)) {
            try {
                Files.createDirectories(targetDir);
            } catch (final FileAlreadyExistsException e) {
                // silently ignore
            } catch (final IOException e) {
                this.logger.debug("Unable to create unpack directory %s", targetDir);
                throw new MojoExecutionException("RPM unpack failed", e);
            }
        }

        try (final RpmInputStream in =
                     new RpmInputStream(new BufferedInputStream(new FileInputStream(this.rpmFile)))) {
            final InputHeader<RpmTag> header = in.getPayloadHeader();
            header.getEntry(RpmTag.FILE_GROUPNAME);
            header.getEntry(RpmTag.FILE_USERNAME);

            final CpioArchiveInputStream cpio = in.getCpioStream();
            CpioArchiveEntry entry;

            while ((entry = cpio.getNextCPIOEntry()) != null) {
                unpackEntry(header, cpio, entry, targetDir);
            }
        } catch (final IllegalArgumentException | IllegalStateException e) {
            this.logger.warn("Bad or insecure RPM file %s", this.rpmFile);
            throw new MojoFailureException("RPM unpack failed, due to bad or insecure RPM file", e);
        } catch (final IOException e) {
            this.logger.debug("Unable to unpack RPM file %s", this.rpmFile);
            throw new MojoFailureException("RPM unpack failed, due to I/O error", e);
        }
    }

    private void unpackEntry(final InputHeader<RpmTag> payloadHeader, final InputStream in,
                             final CpioArchiveEntry entry, final Path targetDir)
            throws IOException {
        if (entry.isDirectory()) {
            final Path directory = makeTargetFile(targetDir, entry.getName());
            if (!Files.exists(directory)) {
                this.logger.debug("Creating directory %s as %s", entry.getName(), directory);
                Files.createDirectories(directory);
            }

            applyFileAttributes(payloadHeader, directory, entry);
        } else if (entry.isRegularFile()) {
            final Path file = makeTargetFile(targetDir, entry.getName());
            this.logger.debug("Unpacking file %s to %s", entry.getName(), file);
            Files.deleteIfExists(file);

            final Path directory = file.getParent();
            if (directory != null && !Files.exists(directory)) {
                this.logger.debug("Creating parent directories: %s", directory);
                Files.createDirectories(directory);
            }

            // write file content
            try (OutputStream os =
                         Files.newOutputStream(file, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                final byte[] buf = new byte[BUFFER_SIZE];
                long remaining = entry.getSize();
                int n;

                while (remaining > 0 && (n = in.read(buf, 0, getReadSize(remaining, buf.length))) > 0) {
                    os.write(buf, 0, n);
                    remaining -= n;
                }
            }

            applyFileAttributes(payloadHeader, file, entry);
        } else if (entry.isSymbolicLink()) {
            final String linkTo = getLinkTarget(payloadHeader, entry.getInode());
            // on supported file systems we could use Files.createSymbolicLink
            this.logger.debug("Ignoring symbolic link %s -> %s", entry.getName(), linkTo);
        } else {
            this.logger.debug("Ignoring entry %s, as it is not a directory, file or symbolic link",
                    entry.getName());
        }
    }

    private static int getReadSize(final long size, final int bufferSize) {
        return size > bufferSize ? bufferSize : (int) size;
    }

    private Path makeTargetFile(final Path parent, final String fileName) {
        this.logger.debug("Checking filename: %s", fileName);

        if (fileName == null || fileName.isEmpty())
            throw new IllegalArgumentException("File name is null or empty");

        if (fileName.length() > MAX_FILENAME_LENGTH)
            throw new IllegalArgumentException(" RPM contains file name that exceeds " + MAX_FILENAME_LENGTH +
                    ", starting with: " + fileName.substring(0, MAX_FILENAME_LENGTH));

        if (fileName.contains("../"))
            throw new IllegalArgumentException("RPM contain relative path: " + fileName);

        if (!FILENAME_PATTERN.matcher(fileName).matches())
            throw new IllegalArgumentException("RPM contains bad characters in file name: " + fileName);

        return parent.resolve(fileName).normalize();
    }

    private void applyFileAttributes(final InputHeader<RpmTag> payloadHeader, final Path path,
                                     final CpioArchiveEntry entry) {
        // first try to preserve last-modification time if desired
        if (this.preserveLastModificationTime) {
            try {
                Files.setLastModifiedTime(path, FileTime.from(entry.getLastModifiedDate().toInstant()));
            } catch (final IOException e) {
                this.logger.debug("Could not preserve last-modification time for %s, due to I/O error: %s",
                        path, e.getMessage());
            }
        }

        // apply file permissions as far as possible
        setFilePermissions(path, entry);

        // attempt to preserve ownership if desired and supported
        if (this.preserveOwner) {
            setFileOwnership(payloadHeader, path, entry);
        }
    }

    private void setFilePermissions(final Path path, final CpioArchiveEntry entry) {
        FileAttributeView view =
                Files.getFileAttributeView(path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if (view == null) {
            view = Files.getFileAttributeView(path, DosFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        }

        if (view instanceof PosixFileAttributeView) {
            try {
                ((PosixFileAttributeView) view).setPermissions(fromMode(entry.getMode()));
            } catch (final IOException e) {
                this.logger.debug("Could not set POSIX file attributes on %s, due to I/O error: %s",
                        path, e.getMessage());
            }
        } else if (view instanceof DosFileAttributeView) {
            this.logger.debug("DOS-compatible File System - permission will be limited to read/write");

            try {
                ((DosFileAttributeView) view).setReadOnly((entry.getMode() & CpioConstants.C_IWUSR) == 0);
            } catch (final IOException e) {
                this.logger.debug("Could not set DOS read/write attributes on %s, due to I/O error: %s",
                        path, e.getMessage());
            }
        } else {
            this.logger.debug("FS does not support POSIX or DOS attributes - using default permissions for %s",
                    path);
        }
    }

    private static Set<PosixFilePermission> fromMode(final long mode) {
        final EnumSet<PosixFilePermission> permissions = EnumSet.noneOf(PosixFilePermission.class);

        // NOTE: SUID, SGID, SVIX are not supported by Java Nio; https://bugs.openjdk.java.net/browse/JDK-8137404

        if ((mode & CpioConstants.C_IRUSR) != 0)
            permissions.add(PosixFilePermission.OWNER_READ);
        if ((mode & CpioConstants.C_IWUSR) != 0)
            permissions.add(PosixFilePermission.OWNER_WRITE);
        if ((mode & CpioConstants.C_IXUSR) != 0)
            permissions.add(PosixFilePermission.OWNER_EXECUTE);

        if ((mode & CpioConstants.C_IRGRP) != 0)
            permissions.add(PosixFilePermission.GROUP_READ);
        if ((mode & CpioConstants.C_IWGRP) != 0)
            permissions.add(PosixFilePermission.GROUP_WRITE);
        if ((mode & CpioConstants.C_IXGRP) != 0)
            permissions.add(PosixFilePermission.GROUP_EXECUTE);

        if ((mode & CpioConstants.C_IROTH) != 0)
            permissions.add(PosixFilePermission.OTHERS_READ);
        if ((mode & CpioConstants.C_IWOTH) != 0)
            permissions.add(PosixFilePermission.OTHERS_WRITE);
        if ((mode & CpioConstants.C_IXOTH) != 0)
            permissions.add(PosixFilePermission.OTHERS_EXECUTE);

        return permissions;
    }

    private void setFileOwnership(final InputHeader<RpmTag> payloadHeader,
                                  final Path path, final CpioArchiveEntry entry) {
        FileOwnerAttributeView view =
                Files.getFileAttributeView(path, PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if (view == null)
            view = Files.getFileAttributeView(path, FileOwnerAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        if (view == null) {
            this.logger.debug("File System does not support ownership");
            return;
        }

        final String userName = getName(payloadHeader, RpmTag.FILE_USERNAME, entry.getUID());
        final UserPrincipalLookupService service;
        final UserPrincipal user;
        try {
            service = path.getFileSystem().getUserPrincipalLookupService();
            user = service.lookupPrincipalByName(userName);
        } catch (final UnsupportedOperationException e) {
            this.logger.debug("Principal Lookup Service not supported: %s", e.getMessage());
            return;
        } catch (final UserPrincipalNotFoundException e) {
            this.logger.debug("User (%s) do not exist - unable to preserve ownership", userName);
            return;
        } catch (final IOException e) {
            this.logger.debug("Could not lookup user (%s) due to I/O error: %s", userName, e.getMessage());
            return;
        }

        if (view instanceof PosixFileAttributeView) {
            final String groupName = getName(payloadHeader, RpmTag.FILE_GROUPNAME, entry.getGID());

            try {
                ((PosixFileAttributeView) view).setGroup(service.lookupPrincipalByGroupName(groupName));
            } catch (final UserPrincipalNotFoundException e) {
                this.logger.debug("Group (%s) do not exist - unable to preserve group ownership", groupName);
            } catch (final IOException e) {
                this.logger.debug("Could not apply group (%s) due to I/O error: %s", groupName, e.getMessage());
            }
        }

        try {
            view.setOwner(user);
        } catch (final IOException e) {
            this.logger.debug("Could not apply user (%s) due to I/O error: %s", userName, e.getMessage());
        }
    }

    private static String getName(final InputHeader<RpmTag> payloadHeader, final RpmTag tag, final long id) {
        final Object values =
                payloadHeader.getEntry(tag)
                        .orElseThrow(() -> new IllegalStateException("RPM lacks " + tag + " lookup table"))
                        .getValue();

        if (!(values instanceof String[])) {
            throw new IllegalStateException("RPM " + tag + " header is not a list of Strings, got " +
                    values.getClass());
        }

        final String[] names = (String[]) values;
        if (id < 0 || names.length <= id) {
            throw new IllegalArgumentException("id out of range [0," + names.length + ']');
        }

        return names[(int) id];
    }

    private static String getLinkTarget(final InputHeader<RpmTag> payloadHeader, final long inode) {
        final Object values =
                payloadHeader.getEntry(RpmTag.FILE_LINKTO)
                        .orElseThrow(() ->
                                new IllegalStateException("RPM contains symbolic link, but lacks linkTo header"))
                        .getValue();

        if (!(values instanceof String[])) {
            throw new IllegalStateException("RPM linkTo header is not a list of Strings, got " + values.getClass());
        }

        final String[] linkTo = (String[]) values;
        if (inode < 0 || linkTo.length <= inode) {
            throw new IllegalArgumentException("Symbolic link inode out of range [0," + linkTo.length + ']');
        }

        return linkTo[(int) inode];
    }

    public void setRpmFile(final File rpmFile) {
        this.rpmFile = rpmFile;
    }

    public void setUnpackDirectory(final File unpackDirectory) {
        this.unpackDirectory = unpackDirectory;
    }

    public void setPreserveLastModificationTime(boolean preserveLastModificationTime) {
        this.preserveLastModificationTime = preserveLastModificationTime;
    }

    public void setPreserveOwner(boolean preserveOwner) {
        this.preserveOwner = preserveOwner;
    }
}
