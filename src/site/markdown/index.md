
# Introduction

The RPM builder Maven plugin allows one to create RPM files without the use of the
`rpmbuild` command line tool, in pure Java. It does work on Linux, Windows, Mac OS X and any other
platform running at least Java 8.
  
## Features

 * Files and directories can be specified manually, one by one, or collected
   from the file system
    
 * Rulesets allow one to apply meta information like "user", "group", "mode", etc
   which are not available in the file system on e.g. the Windows platform

## Versioning

The default way this plugin creates an RPM version from a Maven versions is as follows:
  
 * Non-SNAPSHOT versions are taken as is an get the release `1` assigned.
  
 * The Non-SNAPSHOT release value can be overridden using the parameter `release`.
  
 * SNAPSHOT versions will get the `-SNAPSHOT` removed and get a generated release identifier assigned.
  
 * The release identifier is a combination of the parameter `snapshotReleasePrefix` (defaults to `0.`)
   and the value of the parameter `snapshotBuildId` (defaults to the current timestamp in the format `yyyyMMddHHmm`).
    
 * The parameter "release" has no effect for "-SNAPSHOT" versions. Unless the
   parameter `forceRelease` is set to `true`, in which case the build will always fall back to
   the Non-SNAPSHOT behavior and use the `release` field as it is. 
  
For example will the RPM version for the Maven version `1.0.0` be `1.0.0-1`. And the Maven version
of `1.0.0-SNAPSHOT` will result in `1.0.0-0.201604250101` (depending on the actual date and time).

The result of these rules are  SNAPSHOT releases which is always lower than the final release.
Unless you override using `forceRelease` and and `snapshotBuildId`. 

## Contributing

All contributions are welcome!
  
## See also

 * [RPM home page](http://www.rpm.org ) 
 * [Eclipse Packager™](http://eclipse.org/packager) – the source of the library implementing RPM write support
