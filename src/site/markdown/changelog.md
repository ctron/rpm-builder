# Changelogs

RPMs allow the change log of a package to be included in the distribution file. This may be accessed by `rpm -q --changelog <package>`.

To add changelogs via the plugin the following may be used:

~~~xml
    <changelogs>
        <changelog>
            <date>2024-04-15T10:14:00+01:00</date>
            <author>John Doe</author>
            <text>A new release 1</text>
        </changelog>
        <changelog>
            <timestamp>1712232000</timestamp>
            <author>Jane Doe</author>
            <text>Older Release</text>
        </changelog>
    </changelogs>
~~~

Note that the timestamp entry may be either in [ISO_OFFSET_DATE_TIME](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/format/DateTimeFormatter.html#ISO_OFFSET_DATE_TIME) or in milliseconds as an epoch unix timestamp.
