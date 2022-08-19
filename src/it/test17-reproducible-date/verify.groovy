def generateMD5(File file) {
    def digest = java.security.MessageDigest.getInstance("MD5")
    file.eachByte(4096) { buffer, length ->
        digest.update(buffer, 0, length)
    }
    digest.digest().encodeHex() as String
}

def verify() {
    Process proc = ("rpm -qilpv --dump " + basedir + "/target/test17-1.0.0-0.200901011100.noarch.rpm").execute()
    return proc.in.getText().trim()
}

def result = verify()
println "Verify: " + result

def expectedMd5Sum = "129cf561ac335e8ddc80da20b27dbb5b";
def md5sum = generateMD5(new File(basedir, "target/test17-1.0.0-0.200901011100.noarch.rpm"))
if (md5sum != expectedMd5Sum) {
    System.out.format("RPM MD5 doesn't match -  actual: %s, expected: %s%n", md5sum, expectedMd5Sum);
    return false;
}

return true;
