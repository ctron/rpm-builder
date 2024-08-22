package de.dentrassi.rpm.builder;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PackageEntryTest {
    @Test
    void testValidateNameNull() {
        final PackageEntry entry = new PackageEntry();
        entry.setLinkTo("something-to-link-to");
        assertThatThrownBy(entry::validate).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testValidateNameEmpty() {
        final PackageEntry entry = new PackageEntry();
        entry.setName("");
        entry.setLinkTo("something-to-link-to");
        assertThatThrownBy(entry::validate).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testValidateNoSource() {
        final PackageEntry entry = new PackageEntry();
        entry.setName("some-entry");
        assertThatThrownBy(entry::validate).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testValidateGhostNull() {
        final PackageEntry entry = new PackageEntry();
        entry.setName("some-entry");
        entry.setGhost(null);
        assertThatThrownBy(entry::validate).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testValidateGhostSource() {
        final PackageEntry entry = new PackageEntry();
        entry.setName("some-entry");
        entry.setGhost(Boolean.TRUE);
        assertThatCode(entry::validate).withFailMessage("Ghost entries do not require other sources").doesNotThrowAnyException();
    }

    @Test
    void testValidateMultipleSourcesGhost() {
        final PackageEntry entry = new PackageEntry();
        entry.setName("some-entry");
        entry.setFile(new File("some-file-entry"));
        entry.setGhost(Boolean.TRUE);
        assertThatThrownBy(entry::validate).isInstanceOf(IllegalStateException.class);
    }
}
