def dump ( ) {
	Process proc = ('rpm -q --dump -p ' + basedir.toString().replace(File.separator, "/") + "/target/test18.rpm").execute()
	return proc.in.getText().trim()
}

def actual = dump()
println "Dump: " + actual

def expected = "/etc/test.txt 11 1230807600 a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e 0100600 root root 0 0 0 X"

if (actual != expected) {
	System.out.format("RPM dump doesn't match -  actual: %s, expected: %s%n", actual, expected);
	return false;
}

return true;
