# Payload entry information

Each payload entry (file) in an RPM package has some additional information,
like owner, permissions or whether it is a configuration file.

Since RPM is designed around POSIX/Unix-like operating systems, part of this information
is normally directly pulled in from the file system, although it can be overridden
in the "spec file".

This plugin cannot use the POSIX file information from the filesystem, since it might be
that the plugin is running on a non-POSIX operating system, which cannot provide this sort
of information. 

Therefore all information, either file system related, or RPM package related (like configuration flag)
has to be provided either [explicitly](entry.html) or using [rulesets](rulesets.html).

Both ways however use the same way to specify the information:


    <user>root</user>                   <!-- name of the user -->
    <group>root</group>                 <!-- name of the group -->
    <mode>0644</mode>                   <!-- octal mode -->
    <configuration>true</configuration> <!-- mark as configuration -->
    <missingOk>true</missingOk>         <!-- mark as "missing ok" in combination with configuration -->
    <noreplace>true</noreplace>         <!-- mark as "no replace" in combination with configuration -->
    <documentation>true</documentation> <!-- mark as documentation -->
    <readme>true</readme>               <!-- mark as readme -->
    <ghost>true</ghost>                 <!-- mark as ghost -->
    <verify>…</verify>                  <!-- verification flags -->

Either of these elements may be specified exactly once.
    
## `<user>`

The user element specifies the file owner. It must be the name of the user.

## `<group>`

The group element specifies the file group. It must be the name of the group.

## `<mode>`

Specifies the file permissions in octal notation. A leading zero is allowed but not required.

It is however **not** possible to use a `chmod` string representation like `u=rw,g=r,o=`.

## `<configuration>`

A boolean flag (`true` or `false`) which marks the entry a configuration file.

**Note:** This is only valid for file entries

## `<missingOk>`

A boolean flag (`true` or `false`) which sets the "missing ok" flag.

**Note:** This should only be used in combination with `<configuration>true</configuration>`. 

## `<noreplace>`

A boolean flag (`true` or `false`) which sets the "noreplace" flag.

**Note:** This should only be used in combination with `<configuration>true</configuration>`.

## `<documentation>`

A boolean flag (`true` or `false`) which marks the entry a documentation file.

## `<readme>`

A boolean flag (`true` or `false`) which marks the entry as a README file.

## `<ghost>`

A boolean flag (`true` or `false`) which marks the entry as a ghost entry.

For more information about ghost, see
"[The %ghost Directive](http://ftp.rpm.org/max-rpm/s1-rpm-inside-files-list-directives.html#S3-RPM-INSIDE-FLIST-GHOST-DIRECTIVE)". 

## `<verify>`

Configure verification flags. Also see [verification flags](verify.html).