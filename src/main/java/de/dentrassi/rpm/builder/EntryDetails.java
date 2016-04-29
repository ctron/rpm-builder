/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.rpm.builder;

import org.eclipse.packagedrone.utils.rpm.build.FileInformation;

public class EntryDetails
{
    private Short mode;

    private Boolean configuration;

    private String user;

    private String group;

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

    public void validate ()
    {
    }

    public boolean apply ( final FileInformation info )
    {
        boolean didApply = false;

        if ( this.configuration != null )
        {
            info.setConfiguration ( this.configuration );
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

}
