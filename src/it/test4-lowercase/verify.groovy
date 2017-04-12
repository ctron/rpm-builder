def pb = new ProcessBuilder ( "bash", "-c", "rpm -qlvvp target/foo-bar*.rpm" );
pb.inheritIO ();
def rc = pb.start ().waitFor();

return rc == 0;