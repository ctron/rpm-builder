package de.dentrassi.rpm.builder;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class RpmIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpmIT.class);

    private static final String IMAGE_NAME = "registry.access.redhat.com/ubi9/ubi-minimal:latest";

    private static final String COMMAND = "sleep infinity";

    @Container
    private static final GenericContainer<?> CONTAINER = new GenericContainer<>(DockerImageName.parse(IMAGE_NAME)).withCommand(COMMAND);

    @BeforeAll
    static void setup() throws IOException, InterruptedException {
        Path itPath = Paths.get("target", "it");
        List<Path> paths = FileUtils.listFiles(itPath.toFile(), new String[] { "rpm" }, true).stream().map(File::toPath).collect(Collectors.toList());

        for (Path path : paths) {
            MountableFile source = MountableFile.forHostPath(path);
            Path dest = itPath.relativize(path);
            LOGGER.info("Copying from host path {} to container path {}", source.getResolvedPath(), dest);
            CONTAINER.copyFileToContainer(source, dest.toString());
        }

        ExecResult result = CONTAINER.execInContainer("rpm", "--version");
        LOGGER.info("{}", result.getStdout());
        assertThat(result.getExitCode()).isZero();
    }

    @Test
    void test1() throws IOException, InterruptedException {
        ExecResult result = CONTAINER.execInContainer("rpm", "-qlvvp", "/test1/target/test1.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout().trim()).isNotEmpty();
    }

    @Test
    void test2() throws IOException, InterruptedException {
        ExecResult result = CONTAINER.execInContainer("rpm", "-qlvvp", "/test2/target/test2.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout().trim()).isNotEmpty();
    }

    @Test
    void test3() throws IOException, InterruptedException {
        String expected = "(contains no files)";
        ExecResult result = CONTAINER.execInContainer("rpm", "-qlvvp", "/test3/target/test3.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout()).isEqualToIgnoringNewLines(expected);
    }

    @Test
    void test4Lowercase() throws IOException, InterruptedException {
        ExecResult result = CONTAINER.execInContainer("rpm", "-qlvvp", "/test4-lowercase/target/foo-bar*.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout().trim()).isNotEmpty();
    }

    @Test
    void test4Uppercase() throws IOException, InterruptedException {
        ExecResult result = CONTAINER.execInContainer("rpm", "-qlvvp", "/test4-uppercase/target/Foo-Bar*.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout().trim()).isNotEmpty();
    }

    @Test
    void test5() throws IOException, InterruptedException {
        ExecResult result = CONTAINER.execInContainer("rpm", "-qlvvp", "/test5-outname/target/my-foo-bar.abc.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout().trim()).isNotEmpty();
    }

    @Test
    void test6Scanner() throws IOException, InterruptedException {
        String expected =
                "/usr/share/test6/a.foo\t\n" +
                "/usr/share/test6/include\tdirectory\n" +
                "/usr/share/test6/include/d.bar\t\n" +
                "/usr/share/test6/link_to_a.foo\tsymbolic link to `a.foo'\n";
        ExecResult result = CONTAINER.execInContainer("rpm", "-qp", "--fileclass", "/test6-scanner/target/test6.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void test7Nosrcpkg() throws IOException, InterruptedException {
        String notExpected = "Source RPM  : test7-srcpkg-.*\\.rpm";
        ExecResult result = CONTAINER.execInContainer( "rpm", "-qip", "/test7-nosrcpkg/target/test7.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout()).doesNotContainPattern(notExpected);
    }

    @Test
    void test7Srcpkg() throws IOException, InterruptedException {
        String expected = "Source RPM  : test7-srcpkg-.*\\.rpm";
        ExecResult result = CONTAINER.execInContainer( "rpm", "-qip", "/test7-srcpkg/target/test7.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout()).containsPattern(expected);
    }

    @Disabled
    void test7Srcpkg2() throws IOException, InterruptedException {
        String notExpected = "foo.bar";
        ExecResult result = CONTAINER.execInContainer( "rpm", "-qip", "/test7-srcpkg2/target/test7.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout()).doesNotContain(notExpected);
    }

    private static String flagsForName(String name) throws IOException, InterruptedException {
        ExecResult result = CONTAINER.execInContainer("rpm", "--qf", "%{" + name + "}", "-qp", "/test8-newflags/target/test8.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        return result.getStdout().trim();
    }

    @Test
    void test8() throws IOException, InterruptedException {
        String suggests = flagsForName("suggests");
        LOGGER.info("Suggests: {}", suggests);

        String recommends = flagsForName("recommends");
        LOGGER.info("Recommends: {}", recommends);

        String enhances = flagsForName("enhances");
        LOGGER.info("Enhances: {}", enhances);

        String supplements = flagsForName("supplements");
        LOGGER.info("Supplements: {}", supplements);

        assertThat(suggests).isEqualTo("suggest");
        assertThat(recommends).isEqualTo("recommend");
        assertThat(enhances).isEqualTo("enhance");
        assertThat(supplements).isEqualTo("supplement");
    }

    @Test
    void test10Ddfaultname() throws IOException, InterruptedException {
        ExecResult result = CONTAINER.execInContainer("rpm", "-qlvvp", "/test10-defaultname/target/test10-defaultname-1.0.0-1.noarch.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout().trim()).isNotEmpty();
    }

    @Test
    void test10Legacyname() throws IOException, InterruptedException {
        ExecResult result = CONTAINER.execInContainer("rpm", "-qlvvp", "/test10-legacyname/target/test10-legacyname-1.0.0-1-noarch.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout().trim()).isNotEmpty();
    }

    @Test
    void test11Prefixes() throws IOException, InterruptedException {
        String expected = "/opt:/var/log:";
        ExecResult result = CONTAINER.execInContainer("rpm", "--qf", "[%{Prefixes}:]", "-qp", "/test11-prefixes/target/test11.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout()).isEqualTo(expected);
    }

    @Test
    void test12Disable() throws IOException, InterruptedException {
        ExecResult result = CONTAINER.execInContainer("rpm", "-qlvvp", "/rpm12-disable/target/*.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout()).isEmpty();
    }

    @Test
    void test13Skipentry() throws IOException, InterruptedException {
        ExecResult result = CONTAINER.execInContainer("rpm", "-qlvvp", "/test13-skipentry/target/test13.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout().trim()).isNotEmpty();
    }

    @Test
    void test14Forcerelease() throws IOException, InterruptedException {
        String expected = "(contains no files)";
        ExecResult result = CONTAINER.execInContainer("rpm", "-qlvvp", "/test14-forcerelease/target/test14-forcerelease-4.5.6-1.noarch.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout()).isEqualToIgnoringNewLines(expected);
    }

    @Test
    void test14Primaryname() throws IOException, InterruptedException {
        String expected = "(contains no files)";
        ExecResult result = CONTAINER.execInContainer("rpm", "-qlvvp", "/test14-primaryname/target/test14-primaryname-1.0.0-*.noarch.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout()).isEqualToIgnoringNewLines(expected);
    }

    @Test
    void test14Snapshotname() throws IOException, InterruptedException {
        String expected = "(contains no files)";
        ExecResult result = CONTAINER.execInContainer("rpm", "-qlvvp", "/test14-snapshotname/target/test14-snapshotname-1.2.3-0.[0-9]*.noarch.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout()).isEqualToIgnoringNewLines(expected);
    }

    @Test
    void test15Default() throws IOException, InterruptedException {
        ExecResult result = CONTAINER.execInContainer("rpm", "-Kv", "/test15-default/target/test15.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout()).contains("Header SHA256 digest: OK");
        assertThat(result.getStdout()).contains("Header SHA1 digest: OK");
        assertThat(result.getStdout()).contains("Payload SHA256 digest: OK");
        assertThat(result.getStdout()).contains("MD5 digest: OK");
    }

    @Test
    void test15Md5only() throws IOException, InterruptedException {
        ExecResult result = CONTAINER.execInContainer("rpm", "-Kv", "/test15-md5-only/target/test15.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout()).contains("MD5 digest: OK");
        assertThat(result.getStdout()).doesNotContain("SHA");
    }

    private static String flagsForOptions(String options) throws IOException, InterruptedException {
        ExecResult result = CONTAINER.execInContainer("rpm", "-l", options, "-qp", "/test16-ghost/target/test16.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        return result.getStdout().trim();
    }

    @Test
    void test16Ghost() throws IOException, InterruptedException {
        String allFiles = flagsForOptions("");
        LOGGER.info("All files: {}", allFiles);

        String noghostFiles = flagsForOptions("--noghost");
        LOGGER.info("No ghost files: {}", noghostFiles);

        assertThat(allFiles).isEqualTo("/tmp/ghost-file-entry");
        assertThat(noghostFiles).isEmpty();
    }

    private static String generateMd5(String file) throws IOException, InterruptedException {
        ExecResult result = CONTAINER.execInContainer("md5sum", file);
        assertThat(result.getExitCode()).isZero();
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        return result.getStdout();
    }

    @Test
    void test17ReproducibleDate() throws IOException, InterruptedException {
        String file = "/test17-reproducible-date/target/test17-1.0.0-0.200901011100.noarch.rpm";
        String expected = "93ebadf3ba02fe04ed2365cbc13c489f";
        ExecResult result = CONTAINER.execInContainer("rpm", "-qilpv", "--dump", file);
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        String md5 = generateMd5(file);
        assertThat(md5).isEqualToIgnoringNewLines(expected + "  " + file);
    }

    @Test
    void test17ReproducibleEpoch() throws IOException, InterruptedException {
        String file = "/test17-reproducible-epoch/target/test17-1.0.0-0.197001010000.noarch.rpm";
        String expected = "11162ad70ef55851a3e1375222cd1a4a";
        ExecResult result = CONTAINER.execInContainer("rpm", "-qilpv", "--dump", file);
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        String md5 = generateMd5(file);
        assertThat(md5).isEqualToIgnoringNewLines(expected + "  " + file);
    }

    @Test
    void test18FileDigestDefault() throws IOException, InterruptedException {
        String expected = "/etc/test.txt 11 1230807600 a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e 0100600 root root 0 0 0 X";
        ExecResult result = CONTAINER.execInContainer("rpm", "-q", "--dump", "-p", "/test18-file-digest-default/target/test18.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout()).isEqualToIgnoringNewLines(expected);
    }

    @Test
    void test18FileDigestMd5() throws IOException, InterruptedException {
        String expected = "/etc/test.txt 11 1230807600 b10a8db164e0754105b7a99be72e3fe5 0100600 root root 0 0 0 X";
        ExecResult result = CONTAINER.execInContainer("rpm", "-q", "--dump", "-p", "/test18-file-digest-md5/target/test18.rpm");
                LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout()).isEqualToIgnoringNewLines(expected);
    }

    @Test
    void test19IntermediateDirectories() throws IOException, InterruptedException {
        List<String> expected = Arrays.stream((
                "1 drwxrwxrwx myuser        mygroup        /opt/mycompany/myapp\n" +
                "2 drwxrwxrwx myuser        mygroup        /opt/mycompany/myapp/a\n" +
                "3 drwxrwxrwx myuser        mygroup        /opt/mycompany/myapp/a/b\n" +
                "4 drwxrwxrwx myuser        mygroup        /opt/mycompany/myapp/a/b/c\n" +
                "5 -r-xr-xr-x myuser        mygroup        /opt/mycompany/myapp/a/b/c/foobar\n" +
                "6 drwxr-xr-x root          root           /etc/mycompany/myapp\n" +
                "7 drwxr-xr-x root          root           /etc/mycompany/myapp/defaults\n" +
                "8 ---x--x--x mygeneraluser mygeneralgroup /opt/mycompany/otherapp/a/b/c/foobar\n").split("\n")).collect(Collectors.toList());
        ExecResult result = CONTAINER.execInContainer("rpm", "-q", "--queryformat",  "[%{FILEINODES} %{FILEMODES:perms} %-13{FILEUSERNAME} %-14{FILEGROUPNAME} %{FILENAMES}\\n]", "/test19-intermediate-directories/target/test19.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(Arrays.stream(result.getStdout().split("\n")).sorted().collect(Collectors.toList())).containsExactlyElementsOf(expected);
    }

    @Test
    void test19IntermediateDirectoriesCollect() throws IOException, InterruptedException {
        List<String> expected = Arrays.stream((
                "1 drwxrwxrwx myuser mygroup /opt/mycompany/myapp\n" +
                "2 drwxrwxrwx myuser mygroup /opt/mycompany/myapp/a\n" +
                "3 drwxrwxrwx myuser mygroup /opt/mycompany/myapp/a/b\n" +
                "4 -r-xr-xr-x myuser mygroup /opt/mycompany/myapp/a/b/x/y/foobar\n" +
                "5 drwxrwxrwx myuser mygroup /opt/mycompany/myapp/c\n" +
                "6 drwxrwxrwx myuser mygroup /opt/mycompany/myapp/c/d\n" +
                "7 drwxrwxrwx myuser mygroup /opt/mycompany/myapp/c/d/x\n" +
                "8 drwxrwxrwx myuser mygroup /opt/mycompany/myapp/c/d/x/y\n" +
                "9 -r-xr-xr-x myuser mygroup /opt/mycompany/myapp/c/d/x/y/foobar").split("\n")).collect(Collectors.toList());
        ExecResult result = CONTAINER.execInContainer("rpm", "-q", "--queryformat",  "[%{FILEINODES} %{FILEMODES:perms} %{FILEUSERNAME} %{FILEGROUPNAME} %{FILENAMES}\\n]", "/test19-intermediate-directories-collect/target/test19.rpm");
        LOGGER.info("{}{}", System.lineSeparator(), result.getStdout());
        assertThat(result.getExitCode()).isZero();
        assertThat(Arrays.stream(result.getStdout().split("\n")).map(s -> s.replaceAll("\\s+", " ")).sorted().collect(Collectors.toList())).containsExactlyElementsOf(expected);
    }
}
