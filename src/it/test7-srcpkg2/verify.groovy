
Process rpm = ("rpm -qip " + basedir.toString().replace("\\", "/") + "/target/test7.rpm").execute();

def entries = rpm.in.readLines();

println entries;

return entries.find { it =~ /Source RPM : foo\.bar/ };
