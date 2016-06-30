# Signing

RPM packages can be signed using PGP header signature.

## Configuration

Signing is configured using the `<signature>` element:

    <configuration>
      <signature>
        <keyId>12345678</keyId> <!-- the ID of the key -->
        <keyringFile>${user.home}/.gnupg/secring.gpg</keyringFile> <!-- path to the keyring file -->
        <passphrase>secret</passphrase> <!-- passphrase to unlock the key -->
        
        <hashAlgorithm>SHA1</hashAlgorithm>  <!-- optional : defaults to SHA1 -->
        <skip>false</skip> <!-- optional : defaults to "false", skip signature --> 
      </signature>
      
      <skipSigning>false</skipSigning> <!-- optional, defaults to "false" -->
    </configuration>
    
The keyring file is a standard GPG keyring file. The key ID is the short ID of the key to use for signing.

The hash algorithm can be either `SHA1`, `SHA256` or `SHA512`.

The `<skip>` element can be set to `true` in order to skip signing the package with this signature.

## Hiding credentials

The passphrase should not be store in any `pom.xml` which is possibly checked in to some sort of
source control system. It is possible to use properties and provide this information either from
the command line or the global Maven settings file `.m2/settings.xml`.

    <signature>
        …
        <phassphrase>${my.passphrase}</passphrase>
    </signature>
    
The following command will then provide the passphrase using the command line:

    mvn package -Dmy.passphrase=secret
    
## Conditional signing

Sometime is may be useful to deactivate signing since the private key is not available. This may be case
when packages are signed in a central server which has the private key available, or a privileged user
who has access to the keyfile, but most other users don't.

This can be achieved using various different ways using the `<skip>` element of the `<signature`> element
or the global `<skipSigning>` element. Either in combination with Maven profiles or external properties.

By default if the `<signature>` element is present in the configuration, the information provided
has to be correct. So a missing/unset passphrase or key id will not disable the signing process, but cause
the build to fail.

## Remarks

There are a bunch of different ways to sign an RPM file, the RPM builder plugin currently
only supports PGP header signatures using SHA1 or SHA512.

Although RPM does support multiple signatures for one RPM file, the RPM builder can currently only
create one.