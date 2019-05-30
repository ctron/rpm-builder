/*******************************************************************************
 * Copyright (c) 2016, 2019 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Red Hat Inc - add new file flags
 *     Peter Wilkinson - add skip entry flag
 *******************************************************************************/
package de.dentrassi.rpm.builder;

import java.util.EnumSet;
import java.util.Set;

import org.eclipse.packager.rpm.FileFlags;
import org.eclipse.packager.rpm.build.FileInformation;

public class EntryDetails
{
    private Short mode;

    private Boolean configuration;

    private Boolean documentation;

    private Boolean license;

    private Boolean readme;

    private Boolean ghost;

    private Boolean missingOk;

    private Boolean noreplace;

    private String user;

    private String group;

    private Boolean skip = false;

    public void setMode ( final String mode )
    {
        this.mode = Short.parseShort ( mode, 8 );
    }

    public Short getMode ()
    {
        return this.mode;
    }

    public void setConfiguration ( final Boolean configuration )
    {
        this.configuration = configuration;
    }

    public Boolean getConfiguration ()
    {
        return this.configuration;
    }

    public void setDocumentation ( final Boolean documentation )
    {
        this.documentation = documentation;
    }

    public Boolean getDocumentation ()
    {
        return this.documentation;
    }

    public void setLicense ( final Boolean license )
    {
        this.license = license;
    }

    public Boolean getLicense ()
    {
        return this.license;
    }

    public void setReadme ( final Boolean readme )
    {
        this.readme = readme;
    }

    public Boolean getReadme ()
    {
        return this.readme;
    }

    public void setGhost ( final Boolean ghost )
    {
        this.ghost = ghost;
    }

    public Boolean getGhost ()
    {
        return this.ghost;
    }

    public void setMissingOk ( final Boolean missingOk )
    {
        this.missingOk = missingOk;
    }

    public Boolean getMissingOk ()
    {
        return this.missingOk;
    }

    public void setNoreplace ( final Boolean noreplace )
    {
        this.noreplace = noreplace;
    }

    public Boolean getNoreplace ()
    {
        return this.noreplace;
    }

    public void setUser ( final String user )
    {
        this.user = user;
    }

    public String getUser ()
    {
        return this.user;
    }

    public void setGroup ( final String group )
    {
        this.group = group;
    }

    public String getGroup ()
    {
        return this.group;
    }

    public void setSkip ( final Boolean skip )
    {
        this.skip = skip;
    }

    public Boolean getSkip ()
    {
        return this.skip;
    }

    public void validate ()
    {
    }

    public boolean apply ( final FileInformation info )
    {
        boolean didApply = false;

        if ( this.configuration != null )
        {
            setFlag ( info, FileFlags.CONFIGURATION );
            didApply = true;
        }
        if ( this.documentation != null )
        {
            setFlag ( info, FileFlags.DOC );
            didApply = true;
        }
        if ( this.license != null )
        {
            setFlag ( info, FileFlags.LICENSE );
            didApply = true;
        }
        if ( this.readme != null )
        {
            setFlag ( info, FileFlags.README );
            didApply = true;
        }
        if ( this.ghost != null )
        {
            setFlag ( info, FileFlags.GHOST );
            didApply = true;
        }
        if ( this.missingOk != null )
        {
            setFlag ( info, FileFlags.MISSINGOK );
            didApply = true;
        }
        if ( this.noreplace != null )
        {
            setFlag ( info, FileFlags.NOREPLACE );
            didApply = true;
        }
        if ( this.user != null && !this.user.isEmpty () )
        {
            info.setUser ( this.user );
            didApply = true;
        }
        if ( this.group != null && !this.group.isEmpty () )
        {
            info.setGroup ( this.group );
            didApply = true;
        }
        if ( this.mode != null )
        {
            info.setMode ( this.mode );
            didApply = true;
        }
        return didApply;
    }

    private void setFlag ( final FileInformation info, final FileFlags flag )
    {
        final Set<FileFlags> flags = info.getFileFlags ();
        if ( flags == null )
        {
            info.setFileFlags ( EnumSet.of ( flag ) );
        }
        else
        {
            flags.add ( flag );
        }
    }

}
