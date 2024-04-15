def dump ( ) {
	Process proc = ('rpm -q --dump -p ' + basedir.toString().replace(File.separator, "/") + "/target/test18.rpm").execute()
	return proc.in.getText().trim()
}

def actual = dump()
println "Dump: " + actual

def expected = "/etc/test.txt 11 1230807600 b10a8db164e0754105b7a99be72e3fe5 0100600 root root 0 0 0 X"

if (actual != expected) {
	System.out.format("RPM dump doesn't match -  actual: %s, expected: %s%n", actual, expected);
	return false;
}

return true;
