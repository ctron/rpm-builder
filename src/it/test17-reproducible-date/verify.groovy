def generateMD5(File file) {
    def digest = java.security.MessageDigest.getInstance("MD5")
    file.eachByte( 4096 ) { buffer, length ->
        digest.update( buffer, 0, length )
    }
    digest.digest().encodeHex() as String
}

def verify ( ) {
    Process proc = ("rpm -qilpv --dump " + basedir + "/target/test17-1.0.0-0.200901011100.noarch.rpm").execute()
    return proc.in.getText().trim()
}

def result = verify()
println "Verify: " + result

def md5sum = generateMD5 ( new File ( basedir, "target/test17-1.0.0-0.200901011100.noarch.rpm" ) )
if ( md5sum != "a4ce449e0e1b7b9045a6750f7a46301f" ) {
    System.out.println ( "RPM MD5 doesn't match: " + md5sum );
    return false;
}

return true;