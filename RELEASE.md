# Performing a release

This is my cheat sheet for doing a release.

## Prepare

Ensure that `.m2/settings.xml` contains the GitHub credentials:

    <server>
      <id>github</id>
      <username><!-- github user --></username>
      <password><!-- password --></password>
    </server>

And also your Sonatype account:

    <server>
      <id>ossrh</id>
      <username><!-- sonatype user --></username>
      <password><!-- password --></password>
    </server>


## Do the release

    mvn release:clean release:prepare release:perform

## Maven Central

Head over to http://oss.sonatype.org/ and do the release

## Upload the documentation

    git checkout <tag>
    # prepare and maybe modify site.xml
    mvn site -Ppages
    git checkout master

