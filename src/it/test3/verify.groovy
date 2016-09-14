def pb = new ProcessBuilder ( "bash", "-c", "rpm -qlvvp target/*.rpm" );
pb.inheritIO ();
pb.start ().waitFor();

return true;