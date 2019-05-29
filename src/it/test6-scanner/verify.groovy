
Process rpm = ("rpm -qlp " + basedir + "/target/test6.rpm").execute();

def entries = rpm.in.readLines();

println entries;

return entries.equals( [
    "/usr/share/test6/a.foo",
    "/usr/share/test6/include",
    "/usr/share/test6/include/d.bar"
] );
