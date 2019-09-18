/********************************************************************************
 * Copyright (c) 2019 Oliver Matz and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * Contributors:
 *     Oliver Matz - initial API and implementation
 *******************************************************************************/
package de.dentrassi.rpm.builder;

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.packager.rpm.VerifyFlags;
import org.eclipse.packager.rpm.build.FileInformation;

/**
 * Each member of this class corresponds to one bit in the verify flags.
 * <p>
 * See http://ftp.rpm.org/api/4.14.0/group__rpmvf.html.
 * See https://github.com/ctron/rpm-builder/issues/41
 */
public final class VerifyDetails
{
    private boolean fileDigest;

    private boolean size;

    private boolean linkto;

    private boolean user;

    private boolean group;

    private boolean mtime;

    private boolean mode;

    private boolean rdev;

    private boolean caps;

    /**
     * Corresponds to rpm verify option --nofiledigest.
     *
     * @return true iff fileDigest shall be verified
     */
    public boolean getFileDigest ()
    {
        return this.fileDigest;
    }

    /**
     * Corresponds to rpm verify option --nofiledigest.
     *
     * @param fileDigest
     *            true iff fileDigest shall be verified
     */
    public void setFileDigest ( final boolean fileDigest )
    {
        this.fileDigest = fileDigest;
    }

    /**
     * Corresponds to rpm verify option --nosize.
     *
     * @return true iff file size shall be verified
     */
    public boolean getSize ()
    {
        return this.size;
    }

    /**
     * Corresponds to rpm verify option --nosize.
     *
     * @param size
     *            true iff file size shall be verified
     */
    public void setSize ( final boolean size )
    {
        this.size = size;
    }

    /**
     * Corresponds to rpm verify option --nolinkto.
     *
     * @return true iff linkto shall be verified
     */
    public boolean getLinkto ()
    {
        return this.linkto;
    }

    /**
     * Corresponds to rpm verify option --nolinkto.
     *
     * @param linkto
     *            true iff linkto shall be verified
     */
    public void setLinkto ( final boolean linkto )
    {
        this.linkto = linkto;
    }

    /**
     * Corresponds to rpm verify option --nouser.
     *
     * @return true iff user shall be verified
     */
    public boolean getUser ()
    {
        return this.user;
    }

    /**
     * Corresponds to rpm verify option --nouser.
     *
     * @param user
     *            true iff user shall be verified
     */
    public void setUser ( final boolean user )
    {
        this.user = user;
    }

    /**
     * Corresponds to rpm verify option --nogroup.
     *
     * @return true iff group shall be verified
     */
    public boolean getGroup ()
    {
        return this.group;
    }

    /**
     * Corresponds to rpm verify option --nogroup.
     *
     * @param group
     *            true iff group shall be verified
     */
    public void setGroup ( final boolean group )
    {
        this.group = group;
    }

    /**
     * Corresponds to rpm verify option --nomtime.
     *
     * @return true iff mtime shall be verified
     */
    public boolean getMtime ()
    {
        return this.mtime;
    }

    /**
     * Corresponds to rpm verify option --nomtime.
     *
     * @param mtime
     *            true iff mtime shall be verified
     */
    public void setMtime ( final boolean mtime )
    {
        this.mtime = mtime;
    }

    /**
     * Corresponds to rpm verify option --nomode.
     *
     * @return true iff mode shall be verified
     */
    public boolean getMode ()
    {
        return this.mode;
    }

    /**
     * Corresponds to rpm verify option --nomode.
     *
     * @param mode
     *            true iff mode shall be verified
     */
    public void setMode ( final boolean mode )
    {
        this.mode = mode;
    }

    /**
     * Corresponds to rpm verify option --nordev.
     *
     * @return true iff rdev shall be verified
     */
    public boolean getRdev ()
    {
        return this.rdev;
    }

    /**
     * Corresponds to rpm verify option --nordev.
     *
     * @param rdev
     *            true iff rdev shall be verified
     */
    public void setRdev ( final boolean rdev )
    {
        this.rdev = rdev;
    }

    /**
     * Corresponds to rpm verify option --nocaps.
     *
     * @return true iff caps shall be verified
     */
    public boolean getCaps ()
    {
        return this.caps;
    }

    /**
     * Corresponds to rpm verify option --nocaps.
     *
     * @param caps
     *            true iff caps shall be verified
     */
    public void setCaps ( final boolean caps )
    {
        this.caps = caps;
    }

    /**
     * Apply verification flags to the file information entry.
     *
     * @see EntryDetails#apply(org.eclipse.packager.rpm.build.FileInformation)
     */
    void apply ( final FileInformation info )
    {
        final Set<VerifyFlags> verifyFlags = EnumSet.noneOf ( VerifyFlags.class );
        transfer ( verifyFlags, this.fileDigest, VerifyFlags.MD5 );
        transfer ( verifyFlags, this.size, VerifyFlags.SIZE );
        transfer ( verifyFlags, this.linkto, VerifyFlags.LINKTO );
        transfer ( verifyFlags, this.user, VerifyFlags.USER );
        transfer ( verifyFlags, this.group, VerifyFlags.GROUP );
        transfer ( verifyFlags, this.mtime, VerifyFlags.MTIME );
        transfer ( verifyFlags, this.mode, VerifyFlags.MODE );
        transfer ( verifyFlags, this.caps, VerifyFlags.CAPS );
        info.setVerifyFlags ( verifyFlags );
    }

    private static void transfer ( final Set<VerifyFlags> target, final boolean val, final VerifyFlags flag )
    {
        if ( val )
        {
            target.add ( flag );
        }
    }

    @Override
    public String toString ()
    {
        final StringBuilder ret = new StringBuilder ( "VerifyDetails{" );
        if ( this.fileDigest )
        {
            ret.append ( "fileDigest," );
        }
        if ( this.size )
        {
            ret.append ( "size," );
        }
        if ( this.linkto )
        {
            ret.append ( "linkto," );
        }
        if ( this.user )
        {
            ret.append ( "user," );
        }
        if ( this.group )
        {
            ret.append ( "group," );
        }
        if ( this.mode )
        {
            ret.append ( "mode," );
        }
        if ( this.rdev )
        {
            ret.append ( "rdev," );
        }
        if ( this.caps )
        {
            ret.append ( "caps," );
        }
        ret.append ( "}" );
        return ret.toString ();
    }
}
