# Verification flags

RPM files allow you to verify the integrity if an installed packages using
`rpm -V` (also see: [http://ftp.rpm.org/max-rpm/ch-rpm-verify.html](http://ftp.rpm.org/max-rpm/ch-rpm-verify.html)).

By default all payload entry (file, directory) attributes will be verified.

However it is possible to manually specify which attributes will be verified, on
a per entry basis. This is possible by using the `<verify>` element in the entry:

~~~xml
<entry>
    …
    <verify>
        <user>true</user>
        <group>true</group>
        <fileDigest>false</fileDigest>
        <mode>true</mode>
        …
    </verify>
</entry>
~~~

In this example the following attributes will be checked: user, group, file mode.

**Note:** Only the flags listed **and** having the value `true` will be checked.
          Omitted entries will **not** be checked!

**Note:** The verify structure will be applied atomically. It is not possible to
          override flags from another rule.