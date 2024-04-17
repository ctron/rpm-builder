
Process rpm = ("rpm -qip " + basedir.toString().replace("\\", "/") + "/target/test7.rpm").execute();

def entries = rpm.in.readLines();

println entries;

return ( !entries.isEmpty() && !entries.find { line -> line =~ /Source RPM : test7-.*\.src\.rpm/ } );
