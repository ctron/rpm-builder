def pb = new ProcessBuilder ( "bash", "-c", "rpm -qlvvp target/Foo-Bar*.rpm" );
pb.inheritIO ();
def rc = pb.start ().waitFor();

return rc == 0;