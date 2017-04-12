# Package naming

In versions prior to 0.9.0 the default behavior was to always lowercase the package name.

This behavior has been changed in 0.9.0 which will allow you to control the naming process. However
the default behavior is to leave the package name unchanged.

It is possible to revert to the old behavior by configuring your build like:

    <naming>
        <case>lowercase</case>
    </naming>
