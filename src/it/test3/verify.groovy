def pb = new ProcessBuilder ( "bash", "-c", "rpm -qlvvp target/test3.rpm" );
pb.directory ( basedir )
pb.inheritIO ();
return ( pb.start ().waitFor () == 0 );
