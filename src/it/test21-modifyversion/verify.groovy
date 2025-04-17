
def log () {
    Process proc = ('rpm -qip ' + basedir + "/target/test21-modifyversion-1.0.1.redhat-00001-1.noarch.rpm").execute();
    return proc.in.getText().trim();
}

String text = log()
return text.contains("Version     : 1.0.1.redhat_00001") &&
        text.contains("Source RPM  : test21-modifyversion-1.0.1.redhat_00001-1.src.rpm")
