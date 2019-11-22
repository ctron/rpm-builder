def verify ( ) {
	Process proc = ('rpm -Kv ' + basedir + "/target/test15.rpm").execute()
	return proc.in.getText().trim()
}

def result = verify()
println "Verify: " + result

def m1 = result =~ /MD5 digest\: OK/
def m2 = result =~ /Header SHA1 digest\: OK/
return m1.find() && m2.find()