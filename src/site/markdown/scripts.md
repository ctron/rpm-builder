# Scripts

RPM allows to run scripts at various points.

Also see
 * <https://fedoraproject.org/wiki/Packaging:Scriptlets>
 * <http://www.rpm.org/max-rpm/s1-rpm-specref-scripts.html#S2-RPM-SPECREF-INSTALL-ERASE-SCRIPTS> 

## Script types

The RPM builder current does support the following script types of RPM:

| Type | Tag | Spec Name | Description |
| ----- | ----- | ---------------- | ------------- |
| Before Installation | `<beforeInstallation>` | `%pre` | Run before the installation of the package |
| After Installation | `<afterInstallation>` | `%post` | Run after the installation of the package |
| Before Removal | `<beforeRemoval>` | `%preun` | Run before the removal of the package |
| After Removal | `<afterRemoval>` | `%postun` | Run after the removal of the package |

Please note that RPM has a few things you might not expected from your scripts. For example is it possible
to have multiple RPMs with the same package name, but different versions, installed. This will
still call you "After removal" script, even when there is still one or more other versions of
your package left. Please consult the RPM documentation for more information about scripts.

There are a few other script types and triggered. These are currently not supported by the RPM builder.

**Note: ** In the following examples `<script>` will be used to represent any script type. 

## Basic configuration

Scripts are configured using the normal plugin configuration:

    <configuration>
      …
        <beforeInstallation>…</beforeInstallation>
        …
        <defaultInterpreter>…</defaultInterpreter> <!-- optional -->
      …
    </configuration>

All scripts share the same configuration:

    <script>
       <interpreter>/bin/bash</interpreter> <!-- optional interpreter -->
       
       <!-- either one of the following -->
       <script>echo "foo bar"</script> <!-- direct script content -->
       <file>src/main/resources/someScript.sh</file> <!-- script content from file -->
    </script>
    
It is also possible to use a quicker variant with inline script content:

    <script>
       echo "foo bar"
    </script>
    
Scripts can either be provided as plain text from inside the Maven configuration or
can be read from a file. Specifying both elements (`<script>` and `<file>`) is unsupported.

The `<interpreter>` elements specifies the script interpreter to use. If it is left out, then
the RPM builder will peek into the first line of the script in try to evaluate the script from the
`#!` marker. (`#!/bin/bash` will result in `/bin/bash`).

If this fails, then the value of the `<defaultInterpreter>` element from the main configuration is used,
which defaults to `/bin/sh`.

## Remarks

The RPM builder currently does not support triggers or the `pretrans` script combination.

If using inline scripts, then the XML formatting might add a few extra whitespaces to your
inline script content. Depending on the script language this may be problem. It is possible
to use the `<![CDATA[…]]>` element to work around this. Alternatively you can also use
the external file mechanism. However due to the fact that the RPM builder does not know about the different
script languages it does not trim, strip or clean up scripts in any way.