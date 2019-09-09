/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.rpm.builder;

import org.eclipse.packager.rpm.build.PayloadEntryType;
import org.slf4j.LoggerFactory;

public class Rule extends EntryDetails
{
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger ( Rule.class );

    private When when;

    private boolean last = false;

    public void setWhen ( final When when )
    {
        this.when = when;
    }

    public When getWhen ()
    {
        return this.when;
    }

    public void setLast ( final boolean last )
    {
        this.last = last;
    }

    public boolean isLast ()
    {
        return this.last;
    }

    public boolean matches ( final Object object, final PayloadEntryType type, final String targetName )
    {
        if ( this.when == null )
        {
            logger.debug ( "No 'when'" );
            return true;
        }
        else
        {
            logger.debug ( "Matching 'when': {}", this.when );
            return this.when.matches ( object, type, targetName );
        }
    }

    @Override
    public String toString ()
    {
        return String.format ( "[Rule - when: %s, then: %s]", this.when, super.toString () );
    }
}
