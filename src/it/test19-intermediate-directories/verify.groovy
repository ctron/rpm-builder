def verifyFileInodes() {
    Process proc = ['rpm', '-q', '--queryformat', '[%{FILEINODES} %{FILEMODES:perms} %-13{FILEUSERNAME} %-14{FILEGROUPNAME} %{FILENAMES}\n]', basedir.path + '/target/*.rpm'].execute() | 'sort -n'.execute()
    proc.waitFor()
    return proc.in.getText().trim()
}

def actual = verifyFileInodes()
println "Verify file inodes:\n" + actual

def expected = """\
    1 drwxrwxrwx myuser        mygroup        /opt/mycompany/myapp
    2 drwxrwxrwx myuser        mygroup        /opt/mycompany/myapp/a
    3 drwxrwxrwx myuser        mygroup        /opt/mycompany/myapp/a/b
    4 drwxrwxrwx myuser        mygroup        /opt/mycompany/myapp/a/b/c
    5 -r-xr-xr-x myuser        mygroup        /opt/mycompany/myapp/a/b/c/foobar
    6 drwxr-xr-x root          root           /etc/mycompany/myapp
    7 drwxr-xr-x root          root           /etc/mycompany/myapp/defaults
    8 ---x--x--x mygeneraluser mygeneralgroup /opt/mycompany/otherapp/a/b/c/foobar""".stripIndent()

if (actual != expected) {
    System.out.format("RPM file inodes doesn't match - actual:%n%s%nexpected:%n%s%n", actual, expected);
    return false;
}

return true;
