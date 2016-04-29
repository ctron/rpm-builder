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

import org.apache.maven.plugin.logging.Log;

public class Logger
{
    private final Log log;

    public Logger ( final Log log )
    {
        this.log = log;
    }

    public void debug ( final String format, final Object... values )
    {
        this.log.debug ( String.format ( format, values ) );
    }

    public void info ( final String format, final Object... values )
    {
        this.log.info ( String.format ( format, values ) );
    }
}
