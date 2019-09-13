package de.dentrassi.rpm.builder;

import org.eclipse.packager.rpm.VerifyFlags;
import org.eclipse.packager.rpm.build.FileInformation;

import java.util.EnumSet;
import java.util.Set;

/**
 * Each member of this class corresponds to one bit in the verify flags.
 * See http://ftp.rpm.org/api/4.14.0/group__rpmvf.html.
 * See https://github.com/ctron/rpm-builder/issues/41
 */
public final class VerifyDetails {
   private Boolean md5;
   private Boolean size;
   private Boolean linkto;
   private Boolean user;
   private Boolean group;
   private Boolean mtime;
   private Boolean mode;
   private Boolean rdev;
   private Boolean caps;

   public Boolean getMd5() {
      return md5;
   }

   public void setMd5(Boolean md5) {
      this.md5 = md5;
   }

   public Boolean getSize() {
      return size;
   }

   public void setSize(Boolean size) {
      this.size = size;
   }

   public Boolean getLinkto() {
      return linkto;
   }

   public void setLinkto(Boolean linkto) {
      this.linkto = linkto;
   }

   public Boolean getUser() {
      return user;
   }

   public void setUser(Boolean user) {
      this.user = user;
   }

   public Boolean getGroup() {
      return group;
   }

   public void setGroup(Boolean group) {
      this.group = group;
   }

   public Boolean getMtime() {
      return mtime;
   }

   public void setMtime(Boolean mtime) {
      this.mtime = mtime;
   }

   public Boolean getMode() {
      return mode;
   }

   public void setMode(Boolean mode) {
      this.mode = mode;
   }

   public Boolean getRdev() {
      return rdev;
   }

   public void setRdev(Boolean rdev) {
      this.rdev = rdev;
   }

   public Boolean getCaps() {
      return caps;
   }

   public void setCaps(Boolean caps) {
      this.caps = caps;
   }

   /**
    * @see EntryDetails#apply(org.eclipse.packager.rpm.build.FileInformation)
    * @return true if at least one flag has been set.
    */
   void apply ( final FileInformation info )
   {
      final Set<VerifyFlags> verifyFlags = EnumSet.noneOf(VerifyFlags.class);
      transfer(verifyFlags, this.md5,    VerifyFlags.MD5 );
      transfer(verifyFlags, this.size,   VerifyFlags.SIZE );
      transfer(verifyFlags, this.linkto, VerifyFlags.LINKTO );
      transfer(verifyFlags, this.user,   VerifyFlags.USER );
      transfer(verifyFlags, this.group,  VerifyFlags.GROUP );
      transfer(verifyFlags, this.mtime,  VerifyFlags.MTIME );
      transfer(verifyFlags, this.mode,   VerifyFlags.MODE );
      transfer(verifyFlags, this.caps,   VerifyFlags.CAPS );
      info.setVerifyFlags ( verifyFlags );
   }

   private static void transfer(Set<VerifyFlags> target, Boolean val, VerifyFlags flag)
   {
      if (val == null)
      {
         return;
      }
      if (!val)
      {
         // target.remove(val); // not needed, target cannot contain flag
         return;
      }
      target.add(flag);
   }

   @Override
   public String toString() {
      final StringBuilder ret = new StringBuilder("VerifyDetails{");
      if (md5 != null) {
         ret.append("md5,");
      }
      if (size != null) {
         ret.append("size,");
      }
      if (linkto != null) {
         ret.append("linkto,");
      }
      if (user != null) {
         ret.append("user,");
      }
      if (group != null) {
         ret.append("group,");
      }
      if (mode != null) {
         ret.append("mode,");
      }
      if (rdev != null) {
         ret.append("rdev,");
      }
      if (caps != null) {
         ret.append("caps,");
      }
      ret.append("}");
      return ret.toString();
   }
}
