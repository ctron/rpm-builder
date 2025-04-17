
def clog () {
    Process proc = ('rpm --changelog -qp ' + basedir + "/target/test20-changelog-4.5.6-1.noarch.rpm").execute();
    return proc.in.getText().trim();
}

String text = clog()
return text.contains("A new release 1") && text.contains("Thu Apr 04 2024 Jane Doe")
