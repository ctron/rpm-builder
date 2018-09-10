def prefixes ( ) {
	Process proc = ('rpm --qf [%{Prefixes}:] -qp ' + basedir + "/target/test11.rpm").execute()
	return proc.in.getText().trim()
}

def result = prefixes()
println "Prefixes: " + result


return result ==  "/opt:/var/log:"