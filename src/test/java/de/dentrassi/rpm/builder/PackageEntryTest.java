package de.dentrassi.rpm.builder;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class PackageEntryTest {
    public PackageEntryTest() {
        super();
    }

    @Test
    public void testValidateNameNull() {
        final PackageEntry entry = new PackageEntry();
        entry.setLinkTo("something-to-link-to");

        assertThrows(IllegalStateException.class, () -> entry.validate());
    }

    @Test
    public void testValidateNameEmpty() {
        final PackageEntry entry = new PackageEntry();
        entry.setName("");
        entry.setLinkTo("something-to-link-to");

        assertThrows(IllegalStateException.class, () -> entry.validate());
    }

    @Test
    public void testValidateNoSource() {
        final PackageEntry entry = new PackageEntry();
        entry.setName("some-entry");

        assertThrows(IllegalStateException.class, () -> entry.validate());
    }

    @Test
    public void testValidateGhostNull() {
        final PackageEntry entry = new PackageEntry();
        entry.setName("some-entry");
        entry.setGhost(null);

        // no NullPointerException must be thrown
        assertThrows(IllegalStateException.class, () -> entry.validate());
    }

    @Test
    public void testValidateGhostSource() {
        final PackageEntry entry = new PackageEntry();
        entry.setName("some-entry");
        entry.setGhost(Boolean.TRUE);

        try {
            entry.validate();
        } catch (final RuntimeException e) {
            fail("Ghost entries do not require other sources, got error: " + e.getMessage());
        }
    }

    @Test
    public void testValidateMultipleSourcesGhost() {
        final PackageEntry entry = new PackageEntry();
        entry.setName("some-entry");
        entry.setFile(new File("some-file-entry"));
        entry.setGhost(Boolean.TRUE);

        assertThrows(IllegalStateException.class, () -> entry.validate());
    }
}
