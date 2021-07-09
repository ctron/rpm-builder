myfile=null
basedir.eachDirRecurse{dir -> 
    dir.eachFileMatch(~/.*test14-forcerelease-4.5.6-1.noarch.rpm.*/) { file ->
        myfile=file
    }
}

myfile != null
