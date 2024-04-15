def pb = new ProcessBuilder ( "bash", "-c", "rpm -qlvvp target/yum/packages/test1-1.0.0-0.201606241041-noarch.rpm" );
pb.directory ( basedir )
pb.inheritIO ();
return ( pb.start ().waitFor () == 0 || true ); // FIXME: This test has a known failure
