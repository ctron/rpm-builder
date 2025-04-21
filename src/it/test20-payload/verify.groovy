def verifyPayload() {
    Process proc = ['rpm', '-q', '--queryformat', '%{PAYLOADCOMPRESSOR} %{PAYLOADFLAGS}\n', basedir.toString().replace(File.separator, "/") + '/target/test20.rpm'].execute()
    proc.waitFor()
    return proc.in.getText().trim()
}

def actual = verifyPayload()
println "Verify payload:\n" + actual
def expected = "zstd 19T0L23"

if (actual != expected) {
    System.out.format("RPM payloads don't match - actual:%n%s%nexpected:%n%s%n", actual, expected);
    return false;
}

return true;
