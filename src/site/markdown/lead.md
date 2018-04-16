# RPM Lead

The RPM lead is a (legacy) header to the RPM file. It's content is
superseded by the header dictionary later on in the file. However some
tools still use the the RPM lead (e.g. YUM, DNF).  

Therefore the lead information must be filled as good as possible. Two
important fields are the "architecture" and the "operating system" flag.
Both values get mapped from the OS and ARCH parameters in the extended
header section. Where in the extended header those values are of "string"
type, the RPM lead only has a 16bit integer and less choices. So what
happens is a reduction from a more complex string type to a simpler int type.

Using the string type new architectures and operating systems can the taken
into account easily, however the int field in the lead section doesn't get
extended anymore. The `rpmbuild` tool provides a mapping table in the macros
which help automatically translating from the string to the int version.

However this plugin cannot make use of this file as the file would not be
available on platforms where RPM isn't installed. And the main purpose of
this plugin is to support RPM creating on all platforms without `rpmbuild`
being installed.

## Lead overrides

Therefore the plugin will map architecture and operating system values as
good as possible. However sometimes the mapping will fail and it will
revert back to default values.

If you run into such a case it is possible to manually provide RPM lead
values for architecture and operating system using the the properties
`leadOverrideArchitecture` and `leadOverrideOperatingSystem`.

