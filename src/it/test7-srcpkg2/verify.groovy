
Process rpm = ("rpm -qip " + basedir + "/target/test7.rpm").execute();

def entries = rpm.in.readLines();

println entries;

return !entries.find { it == "foo.bar" };
