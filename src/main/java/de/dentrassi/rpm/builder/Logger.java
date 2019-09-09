/*******************************************************************************
 * Copyright (c) 2016, 2018 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Red Hat Inc - add log level
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

    public void warn ( final String format, final Object... values )
    {
        this.log.warn ( String.format ( format, values ) );
    }
}
