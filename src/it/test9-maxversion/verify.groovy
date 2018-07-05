
def flags ( name ) {
	Process proc = ('rpm --qf %{' + name + '} -qp ' + basedir + "/target/test8.rpm").execute();
	return proc.in.getText().trim();
}

def suggests = flags("suggests");
println "Suggests: " + suggests;

def recommends = flags("recommends");
println "Recommends: " + recommends;

def enhances = flags("enhances");
println "Enhances: " + enhances;

def supplements = flags("supplements");
println "Supplements: " + supplements;

return
	suggests ==  "suggest" &&
	recommends == "recommend" &&
	enhances == "enhance" &&
	supplements == "supplement"
	;