
def log () {
    Process proc = ['rpm', '-q', '--queryformat', '[%{VERSION} %{SOURCERPM}]', basedir.toString() + "/target/test21-modifyversion-1.0.1.redhat-00001-1.noarch.rpm"].execute();
    return proc.in.getText().trim();
}

String text = log()
return text.contains("1.0.1.redhat_00001 test21-modifyversion-1.0.1.redhat_00001-1.src.rpm")
