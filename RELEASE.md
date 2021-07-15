# Performing a release

This is my cheat sheet for doing a release.

## Do the release

    mvn release:clean release:prepare release:perform

## Maven Central

Head over to http://oss.sonatype.org/ and do the release

## Upload the documentation

    git checkout <tag>
    # prepare and maybe modify site.xml
    mvn site:site site:staging
    git checkout gh-pages
    cp target/* . -a
    git commit -a
    git push
    git checkout master
