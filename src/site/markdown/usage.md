# Usage

The following sections show how the RPM builder can be used.
  
## Adding a single file

The following configuration will add a single file, named `/etc/foo/bar.conf` on the target
machine and `src/main/resources/conf/bar.cfg` during the build. The file will be added "as is",
no further processing will be done.
  
The file information assigned with this file, will be the default rule set. File information,
permissions etc stored in the file system will be ignored.

    …
    <configuration>
      …
      <entries>
        <entry>
          <name>/etc/foo/bar.conf</name> <!-- target name -->
          <file>src/main/resources/conf/bar.cfg</file> <!-- source file -->
        </entry>
      </entries>
      …
    </configuration>
    …

The following snippet will use the provided file information instead.

    …
    <configuration>
      …
      <entries>
        <entry>
          <name>/etc/foo/bar.conf</name> <!-- target name -->
          <file>src/main/resources/conf/bar.cfg</file> <!-- source file -->
          
          <user>user</user>
          <group>group</group>
          <mode>0640</mode> <!-- u=rw,g=r,o= -->
          <configuration>true</configuration>
        </entry>
      </entries>
      …
    </configuration>
    …


## Adding an explicit directory

The following snippet will create an explicit directory. This directory will be
removed when the RPM is being removed from the target system.

    …
    <configuration>
      …
        <entries>
          <entry>
            <name>/etc/foo</name> <!-- target name -->
            <directory>true</directory> <!-- flag as directory -->
          </entry>
        </entries>
        …
    </configuration>
    …
