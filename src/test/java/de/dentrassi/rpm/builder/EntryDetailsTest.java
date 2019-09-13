package de.dentrassi.rpm.builder;

import org.eclipse.packager.rpm.FileFlags;
import org.eclipse.packager.rpm.build.FileInformation;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertArrayEquals;

/**
 * Test class de.dentrassi.rpm.builder.EntryDetails.
 */
public class EntryDetailsTest
{
   /**
    * Verify that empty {@link EntryDetails} result in empty set of  {@link FileFlags}.
    */
   @Test
   public void applyEmpty()
   {
      final EntryDetails entryDetails = new EntryDetails();
      doTest(new FileFlags[] {}, entryDetails);
   }

   /**
    * Verify that {@link EntryDetails#setReadme(java.lang.Boolean)} correctly controls {@link FileFlags#README}.
    */
   @Test
   public void applyReadmeTrue()
   {
      final EntryDetails entryDetails = new EntryDetails();
      entryDetails.setReadme(true);
      doTest(new FileFlags[] {FileFlags.README}, entryDetails);
   }

   /**
    * False negative? See https://github.com/ctron/rpm-builder/issues/42
    */
   @Test
   public void applyReadmeFalse()
   {
      final EntryDetails entryDetails = new EntryDetails();
      entryDetails.setReadme(false);
      doTest(new FileFlags[] {FileFlags.README}, entryDetails); // questionable
   }

   private static void doTest(FileFlags[] expectedResult, final EntryDetails entryDetails)
   {
      final FileInformation fileInformation = new FileInformation();
      entryDetails.apply(fileInformation);
      final Set<FileFlags> fileFlags = fileInformation.getFileFlags();
      assertArrayEquals(expectedResult, fileFlags.toArray());
   }
}