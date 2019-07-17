import java.io.File

myfile=null
basedir.eachDirRecurse{dir -> 
    dir.eachFileMatch(~/.*test14-primaryname-1.0.0-0.\d+.noarch.rpm.*/) { file ->
        myfile=file
    }
}

myfile != null
