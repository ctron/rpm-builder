package de.dentrassi.rpm.builder;

import org.eclipse.packager.rpm.FileFlags;
import org.eclipse.packager.rpm.VerifyFlags;
import org.eclipse.packager.rpm.build.FileInformation;
import org.junit.Test;

import java.util.Set;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertArrayEquals;

/**
 * See https://github.com/ctron/rpm-builder/issues/41
 */
public class VerifyDetailsTest
{
   /**
    * Verify that empty {@link VerifyDetails} result in empty set of {@link VerifyFlags}.
    */
   @Test
   public void applyEmpty()
   {
      final VerifyDetails verifyDetails = new VerifyDetails();
      doTest(new VerifyFlags[] {}, verifyDetails);
   }

   @Test
   public void applyUserOrGroupy()
   {
      final VerifyDetails verifyDetails = new VerifyDetails();
      verifyDetails.setUser(true);
      verifyDetails.setGroup(true);
      doTest(new VerifyFlags[] {VerifyFlags.USER, VerifyFlags.GROUP}, verifyDetails);
   }

   /**
    * Verify that {@link VerifyDetails#setLinkto(java.lang.Boolean)} with value true correctly controls {@link VerifyFlags#LINKTO}.
    */
   @Test
   public void applyLinktoTrue()
   {
      final VerifyDetails verifyDetails = new VerifyDetails();
      verifyDetails.setLinkto(true);
      doTest(new VerifyFlags[] {VerifyFlags.LINKTO}, verifyDetails);
   }

   /**
    * Verify that {@link VerifyDetails#setLinkto(java.lang.Boolean)} with value false does not influence {@link VerifyFlags#LINKTO}.
    * @see EntryDetailsTest#applyReadmeFalse()
    */
   @Test
   public void applyLinktoFalse()
   {
      final VerifyDetails verifyDetails = new VerifyDetails();
      verifyDetails.setLinkto(false);
      doTest(new VerifyFlags[] {}, verifyDetails);
   }

   /**
    * Verify that not calling {@link VerifyDetails#apply(org.eclipse.packager.rpm.build.FileInformation)} results
    * in a null VerifyDetails (as opposed to an empty set).
    */
   @Test
   public void noApply()
   {
      final FileInformation fileInformation = new FileInformation();
      final Set<VerifyFlags> verifyFlags = fileInformation.getVerifyFlags();
      assertNull(verifyFlags);
   }

   private static void doTest(VerifyFlags[] expectedResult, final VerifyDetails verifyDetails)
   {
      final FileInformation fileInformation = new FileInformation();
      verifyDetails.apply(fileInformation);
      final Set<VerifyFlags> verifyFlags = fileInformation.getVerifyFlags();
      assertArrayEquals(expectedResult, verifyFlags.toArray());
   }
}
