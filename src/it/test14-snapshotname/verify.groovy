import java.io.File

myfile=null
basedir.eachDirRecurse{dir -> 
    dir.eachFileMatch(~/.*test14-snapshotname-1.2.3-0.\d+.noarch.rpm.*/) { file ->
        myfile=file
    }
}

myfile != null
