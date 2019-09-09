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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import com.google.common.base.Strings;
import com.google.common.io.CharStreams;

public class Script
{
    private String interpreter;

    private File file;

    private String script;

    public String getInterpreter ()
    {
        return this.interpreter;
    }

    public void setInterpreter ( final String interpreter )
    {
        this.interpreter = interpreter;
    }

    public File getFile ()
    {
        return this.file;
    }

    public void setFile ( final File file )
    {
        this.file = file;
    }

    public String getScript ()
    {
        return this.script;
    }

    public void setScript ( final String script )
    {
        this.script = script;
    }

    public void set ( final String script )
    {
        setScript ( script );
    }

    public String makeScriptContent () throws IOException
    {
        if ( this.file != null && !Strings.isNullOrEmpty ( this.script ) )
        {
            throw new IllegalStateException ( "Script must not have 'file' and 'script' set at the same time." );
        }

        if ( this.file != null )
        {
            try ( Reader reader = new InputStreamReader ( new FileInputStream ( this.file ), StandardCharsets.UTF_8 ) )
            {
                return CharStreams.toString ( reader );
            }
        }

        return this.script;
    }
}
