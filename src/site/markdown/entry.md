# Entries

The actual payload section of an RPM is made up by the `<entry>` elements.

Also see the [usage](usage.html) page.

## Basic structure

All `<entry>` elements have to be contained in an `<entries>` element in the
`<configuration>` element of the plugin or the execution of the plugin:

    <configuration>
      <entries>
        <entry>…</entry>
        <entry>…</entry>
        <entry>…</entry>
      </entries>
    </configuration>
  
Each `<entry>` element has the common child element `<name>`, which defines
the name of the element on the target system. This element is required.

Since version `1.2.0`, every `<entry>` element has the optional `<skip>`
child element. This allows to disable an entry (`true` will skip the entry,
the default is `false`). The value can also be provided with a property.

## Entry type / source

There are a few different entry types, defining where the actual payload data comes from for this entry.

Each entry must only have one type set. Setting multiple types for one entry will fail the build.

### Explicit directory

In order to add a directory entry use: `<directory>true</directory>`.
There is no need for additional source information.

### Single file

Adding a single file is done by: `<file>path/to/file</file>`. The path to the file is relative
to the Maven project.

### Symbolic link

Adding a single file is done by: `<linkTo>link/target</linkTo>`. The path where the
symbolic link points to. If this link to path is relative, then it is relative on the target system.

### File system collector

In order to walk through a directory tree and add all files use: `<collect>…</collect>`.

The collect elements requires one additional element: `<from>` which defines the base path. In addition
there is the optional element `<directories>`, which can be set to `false` in order to not record
directories explicitly. **Note:** the `<from>` directory itself will never be added as explicit directory. This can be done using an additional `<entry>` element.

**Note:** By default symbolic links in the file system will be ignored. Since not all platforms support
symbolic links in the same way. It is recommended to create the manually using a `<linkTo>` style
entry. This behavior can be changed by changed by adding `<symbolicLinks>true</symbolicLinks>` to the
collector configuration.  

The target file names will be constructed out the entry name, as base prefix, and the relative
filename of the file found.

Assuming you have the following director tree in the file system:

    src/main/resources/dir1/foo1.txt
    src/main/resources/dir1/foo2.txt
    src/main/resources/dir2/foo3.txt
    src/main/resources/dir2/foo4.txt
    src/main/resources/dir2/foo5.txt -> foo4.txt (symlink)

Using the collector configuration:

    <entry>
      <name>/usr/lib/foo</name>
      <collect>
        <from>src/main/resources</from>
      </collect>
    </entry> 

Will result in the following payload entries:

    /usr/lib/foo/dir1            (dir)
    /usr/lib/foo/dir1/foo1.txt   (file)
    /usr/lib/foo/dir1/foo2.txt   (file)
    /usr/lib/foo/dir2            (dir)
    /usr/lib/foo/dir2/foo3.txt   (file)
    /usr/lib/foo/dir2/foo4.txt   (file)

As of version 1.0.0 it is also possible to use the standard Maven `includes`/`excludes` elements
which follow the standard Maven include/exclude pattern. For example:

    <entry>
        <name>/usr/lib/foo</name>
        <collect>
            <from>src/main/resources</from>
            <includes>
                <include>…</include>
            </includes>
            <excludes>
                <exclude>…</exclude>
            </excludes>
        </collect>
    </entry>

## Entry information

Each payload entry has additional information such as "user", "group", "mode", "configuration", ...

This information is **not** read from the filesystem, since this may not work on some platforms.
RPM does assume a POSIX like system, so building on a Windows system may not provide enough information
to properly fill out all required information in the file.

Entry information can be specified either explicitly, with each entry, or be evaluated based on [rulesets](rulesets.html).

If neither explicit information, nor a proper ruleset is provided, then the behavior is undefined, but
resorts to some useful defaults.

It is possible to mix the use of rulesets and explicit information. In this case the ruleset will be evaluated
first and the explicit information will override the ruleset result.

### Ruleset

In order to use a ruleset add the `<ruleset>` element:

    <entry>
       <name>…</name>
       …
       <ruleset>my-default</ruleset>
    </entry>

### Explicit

In order to use explicit information use the following elements:

    <entry>
       <name>…</name>
       …
       <user>root</user>                   <!-- name of the user -->
       <group>root</group>                 <!-- name of the group -->
       <mode>0644</mode>                   <!-- octal mode -->
       <configuration>true</configuration> <!-- mark as configuration -->
       <…>…</…>
    </entry>

Also see [payload entry information](payload_information.html) for a full list.
