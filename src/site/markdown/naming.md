# Package naming

This section offers a few options about the naming of packages.

## Case sensitivity

In versions prior to 0.9.0 the default behavior was to always lowercase the package name.

This behavior has been changed in 0.9.0 which will allow you to control the naming process. However
the default behavior is to leave the package name unchanged.

It is possible to revert to the old behavior by configuring your build like:

    <naming>
        …
        <case>lowercase</case>
        …
    </naming>

## Default file name format

Originally the RPM library backing this Maven plugin created a default RPM file name
in the structure of `<name>-<version>-<arch>.rpm`, which wasn't the default that RPM
used. It was possible to override this, but starting with version 0.11.0 of the Maven plugin
the default name was changed to `<name>-<version>.<arch>.rpm`.

However the old (legacy) format can still be used by configuring the naming:

    <naming>
        …
        <defaultFormat>legacy</defaultFormat>
        …
    </naming>

**Note:** This only controls the default name, you can still override the file name by explicitly
configuring a name.
