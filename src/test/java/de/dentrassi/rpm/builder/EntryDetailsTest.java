package de.dentrassi.rpm.builder;

import org.eclipse.packager.rpm.FileFlags;
import org.eclipse.packager.rpm.build.FileInformation;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
      doTest(new FileFlags[] {}, false, entryDetails);
   }

   /**
    * Verify that {@link EntryDetails#setReadme(java.lang.Boolean)} correctly controls {@link FileFlags#README}.
    */
   @Test
   public void applyReadmeTrue()
   {
      final EntryDetails entryDetails = new EntryDetails();
      entryDetails.setReadme(true);
      doTest(new FileFlags[] {FileFlags.README}, true, entryDetails);
   }

   /**
    * False negative? See https://github.com/ctron/rpm-builder/issues/42
    */
   @Test
   public void applyReadmeFalse()
   {
      final EntryDetails entryDetails = new EntryDetails();
      entryDetails.setReadme(false);
      doTest(new FileFlags[] {FileFlags.README}, true, entryDetails); // questionable
   }

   /**
    * invokes {@link EntryDetails#apply(org.eclipse.packager.rpm.build.FileInformation)}
    * @param expectedResult expected return value of {@link FileInformation#getFileFlags()}
    * @param expectedApplied expected return value of {@link de.dentrassi.rpm.builder.EntryDetails#apply(org.eclipse.packager.rpm.build.FileInformation)}
    */
   private static void doTest(FileFlags[] expectedResult, boolean expectedApplied, final EntryDetails entryDetails)
   {
      final FileInformation fileInformation = new FileInformation();
      assertEquals(expectedApplied, entryDetails.apply(fileInformation));
      final Set<FileFlags> fileFlags = fileInformation.getFileFlags();
      assertArrayEquals(expectedResult, fileFlags.toArray());
   }
}