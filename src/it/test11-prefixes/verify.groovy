
def prefixes ( ) {
	Process proc = ('rpm --qf "[%{Prefixes}\n]" -qp ' + basedir + "/target/test11.rpm").execute();
	return proc.in.getText().trim();
}

def result = prefixes();
println "Prefixes: " + result;


return result ==  "/opt\n/var/log\n";