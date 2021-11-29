
def flags ( options ) {
	Process proc = ('rpm -l ' + options + ' -qp ' + basedir + "/target/test16.rpm").execute();
	return proc.in.getText().trim();
}

def all_files = flags("");
println "Suggests: " + all_files;

def noghost_files = flags("--noghost");
println "Recommends: " + noghost_files;

return
	all_files ==  "./tmp/ghost-file-entry" &&
	noghost_files == ""
	;
