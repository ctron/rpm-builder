def verifyFileInodes() {
    Process proc = ['rpm', '-q', '--queryformat', '[%{FILEINODES} %{FILEMODES:perms} %{FILEUSERNAME} %{FILEGROUPNAME} %{FILENAMES}\n]', basedir.path + '/target/*.rpm'].execute() | 'sort -n'.execute()
    proc.waitFor()
    return proc.in.getText().trim()
}

def actual = verifyFileInodes()
println "Verify file inodes:\n" + actual

def expected = """\
    1 drwxrwxrwx myuser mygroup /opt/mycompany/myapp
    2 drwxrwxrwx myuser mygroup /opt/mycompany/myapp/a
    3 drwxrwxrwx myuser mygroup /opt/mycompany/myapp/a/b
    4 -r-xr-xr-x myuser mygroup /opt/mycompany/myapp/a/b/x/y/foobar
    5 drwxrwxrwx myuser mygroup /opt/mycompany/myapp/c
    6 drwxrwxrwx myuser mygroup /opt/mycompany/myapp/c/d
    7 drwxrwxrwx myuser mygroup /opt/mycompany/myapp/c/d/x
    8 drwxrwxrwx myuser mygroup /opt/mycompany/myapp/c/d/x/y
    9 -r-xr-xr-x myuser mygroup /opt/mycompany/myapp/c/d/x/y/foobar""".stripIndent()

if (actual != expected) {
    System.out.format("RPM file inodes doesn't match - actual:%n%s%nexpected:%n%s%n", actual, expected);
    return false;
}

return true;
