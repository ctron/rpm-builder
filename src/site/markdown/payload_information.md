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

Both ways however use the same to specify the information:


    <user>root</user>                   <!-- name of the user -->
    <group>root</group>                 <!-- name of the group -->
    <mode>0644</mode>                   <!-- octal mode -->
    <configuration>true</configuration> <!-- mark as configuration -->
    
Either of these elements may be specified exactly once.
    
## `<user>`

The user element specifies the file owner. It must be the name of the user.

## `<group>`

The group element specifies the file group. It must be the name of the group.

## `<mode>`

Specifies the file permissions in octal notation. A leading zero is allowed but not required.

It is however **not** possible to use a `chmod` like string representation like `u=rw,g=r,o=`.

## `<configuration>`

A boolean flag (`true` or `false`) which marks the entry a configuration file.

**Note:** This is only valid for file entries