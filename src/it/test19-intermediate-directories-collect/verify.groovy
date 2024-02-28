def dump() {
    Process proc = ('rpm -q --dump -p ' + basedir + "/target/*.rpm").execute()
    return proc.in.getText().trim()
}

def actual = dump()
println "Dump:\n" + actual

def expected = """\
    /opt/mycompany/myapp 0 1230807600 0000000000000000000000000000000000000000000000000000000000000000 040777 myuser mygroup 0 0 0 X
    /opt/mycompany/myapp/a 0 1230807600 0000000000000000000000000000000000000000000000000000000000000000 040777 myuser mygroup 0 0 0 X
    /opt/mycompany/myapp/a/b 0 1230807600 0000000000000000000000000000000000000000000000000000000000000000 040777 myuser mygroup 0 0 0 X
    /opt/mycompany/myapp/a/b/x/y/foobar 0 1230807600 e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855 0100555 myuser mygroup 0 0 0 X
    /opt/mycompany/myapp/c 0 1230807600 0000000000000000000000000000000000000000000000000000000000000000 040777 myuser mygroup 0 0 0 X
    /opt/mycompany/myapp/c/d 0 1230807600 0000000000000000000000000000000000000000000000000000000000000000 040777 myuser mygroup 0 0 0 X
    /opt/mycompany/myapp/c/d/x 0 1230807600 0000000000000000000000000000000000000000000000000000000000000000 040777 myuser mygroup 0 0 0 X
    /opt/mycompany/myapp/c/d/x/y 0 1230807600 0000000000000000000000000000000000000000000000000000000000000000 040777 myuser mygroup 0 0 0 X
    /opt/mycompany/myapp/c/d/x/y/foobar 0 1230807600 e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855 0100555 myuser mygroup 0 0 0 X""".stripIndent()

if (actual != expected) {
    System.out.format("RPM dump doesn't match - actual:%n%s%nexpected:%n%s%n", actual, expected);
    return false;
}

return true;
