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

import org.eclipse.packagedrone.utils.rpm.build.PayloadEntryType;
import org.slf4j.LoggerFactory;

public class When
{
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger ( When.class );

    private String type;

    private String prefix;

    public void setType ( final String type )
    {
        this.type = type;
    }

    public String getType ()
    {
        return this.type;
    }

    public void setPrefix ( final String prefix )
    {
        this.prefix = prefix;
    }

    public String getPrefix ()
    {
        return this.prefix;
    }

    @Override
    public String toString ()
    {
        return String.format ( "[when - type: %s, prefix: %s]", this.type, this.prefix );
    }

    public boolean matches ( final Object object, final PayloadEntryType type, final String targetName )
    {
        if ( this.prefix != null && !this.prefix.isEmpty () && !targetName.startsWith ( this.prefix ) )
        {
            logger.debug ( "Prefix is set and does not match - expected: '{}', provided: '{}'", this.prefix, targetName );
            return false;
        }

        if ( this.type != null && !this.type.isEmpty () )
        {
            logger.debug ( "Testing type - expected: {}, actual: {}", this.type, type );

            switch ( this.type.toLowerCase () )
            {
                case "directory":
                    if ( type != PayloadEntryType.DIRECTORY )
                    {
                        return false;
                    }
                    break;
                case "file":
                    if ( type != PayloadEntryType.FILE )
                    {
                        return false;
                    }
                    break;
                case "link":
                    if ( type != PayloadEntryType.SYMBOLIC_LINK )
                    {
                        return false;
                    }
                    break;
                default:
                    throw new IllegalStateException ( String.format ( "Unknown match type: '%s'", this.type ) );
            }
        }

        logger.debug ( "Is a match" );

        return true;
    }
}
