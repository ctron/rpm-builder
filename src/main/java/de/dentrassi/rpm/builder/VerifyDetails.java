/*
 * Copyright (c) 2019 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * Contributors:
 *     Oliver Matz - initial API and implementation
 *******************************************************************************/
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
   private Boolean fileDigest;
   private Boolean size;
   private Boolean linkto;
   private Boolean user;
   private Boolean group;
   private Boolean mtime;
   private Boolean mode;
   private Boolean rdev;
   private Boolean caps;

   /**
    * Corresponds to rpm verify option --nofiledigest.
    */
   public Boolean getFileDigest() {
      return fileDigest;
   }

   /**
    * Corresponds to rpm verify option --nofiledigest.
    */
   public void setFileDigest(Boolean fileDigest) {
      this.fileDigest = fileDigest;
   }

   /**
    * Corresponds to rpm verify option --nosize.
    */
   public Boolean getSize() {
      return size;
   }

   /**
    * Corresponds to rpm verify option --nosize.
    */
   public void setSize(Boolean size) {
      this.size = size;
   }

   /**
    * Corresponds to rpm verify option --nolinkto.
    */
   public Boolean getLinkto() {
      return linkto;
   }

   /**
    * Corresponds to rpm verify option --nolinkto.
    */
   public void setLinkto(Boolean linkto) {
      this.linkto = linkto;
   }

   /**
    * Corresponds to rpm verify option --nouser.
    */
   public Boolean getUser() {
      return user;
   }

   /**
    * Corresponds to rpm verify option --nouser.
    */
   public void setUser(Boolean user) {
      this.user = user;
   }

   /**
    * Corresponds to rpm verify option --nogroup.
    */
   public Boolean getGroup() {
      return group;
   }

   /**
    * Corresponds to rpm verify option --nogroup.
    */
   public void setGroup(Boolean group) {
      this.group = group;
   }

   /**
    * Corresponds to rpm verify option --nomtime.
    */
   public Boolean getMtime() {
      return mtime;
   }

   /**
    * Corresponds to rpm verify option --nomtime.
    */
   public void setMtime(Boolean mtime) {
      this.mtime = mtime;
   }

   /**
    * Corresponds to rpm verify option --nomode.
    */
   public Boolean getMode() {
      return mode;
   }

   /**
    * Corresponds to rpm verify option --nomode.
    */
   public void setMode(Boolean mode) {
      this.mode = mode;
   }

   /**
    * Corresponds to rpm verify option --nordev.
    */
   public Boolean getRdev() {
      return rdev;
   }

   /**
    * Corresponds to rpm verify option --nordev.
    */
   public void setRdev(Boolean rdev) {
      this.rdev = rdev;
   }

   /**
    * Corresponds to rpm verify option --nocaps.
    */
   public Boolean getCaps() {
      return caps;
   }

   /**
    * Corresponds to rpm verify option --nocaps.
    */
   public void setCaps(Boolean caps) {
      this.caps = caps;
   }

   /**
    * @see EntryDetails#apply(org.eclipse.packager.rpm.build.FileInformation)
    */
   void apply ( final FileInformation info )
   {
      final Set<VerifyFlags> verifyFlags = EnumSet.noneOf(VerifyFlags.class);
      transfer(verifyFlags, this.fileDigest, VerifyFlags.MD5 );
      transfer(verifyFlags, this.size,       VerifyFlags.SIZE );
      transfer(verifyFlags, this.linkto,     VerifyFlags.LINKTO );
      transfer(verifyFlags, this.user,       VerifyFlags.USER );
      transfer(verifyFlags, this.group,      VerifyFlags.GROUP );
      transfer(verifyFlags, this.mtime,      VerifyFlags.MTIME );
      transfer(verifyFlags, this.mode,       VerifyFlags.MODE );
      transfer(verifyFlags, this.caps,       VerifyFlags.CAPS );
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
      if (fileDigest != null) {
         ret.append("fileDigest,");
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
