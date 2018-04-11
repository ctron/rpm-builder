import java.io.File
import java.nio.file.Files

import groovy.io.FileType

def found = false
def parent = new File(basedir, "target")

parent.eachFileRecurse(FileType.FILES) { file ->
    if ( file.name.startsWith("Foo-Bar")) {
        found = true
    }
}

return found