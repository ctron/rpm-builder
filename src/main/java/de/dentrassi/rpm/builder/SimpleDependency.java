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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.packager.rpm.deps.RpmDependencyFlags;

public class SimpleDependency
{
    protected static final Map<String, RpmDependencyFlags[]> OP_MAP;

    static
    {
        OP_MAP = new HashMap<> ();

        OP_MAP.put ( "=", new RpmDependencyFlags[] { RpmDependencyFlags.EQUAL } );
        OP_MAP.put ( ">=", new RpmDependencyFlags[] { RpmDependencyFlags.EQUAL, RpmDependencyFlags.GREATER } );
        OP_MAP.put ( "<=", new RpmDependencyFlags[] { RpmDependencyFlags.EQUAL, RpmDependencyFlags.LESS } );
        OP_MAP.put ( ">", new RpmDependencyFlags[] { RpmDependencyFlags.GREATER } );
        OP_MAP.put ( "<", new RpmDependencyFlags[] { RpmDependencyFlags.LESS } );

        OP_MAP.put ( "eq", new RpmDependencyFlags[] { RpmDependencyFlags.EQUAL } );
        OP_MAP.put ( "ge", new RpmDependencyFlags[] { RpmDependencyFlags.EQUAL, RpmDependencyFlags.GREATER } );
        OP_MAP.put ( "le", new RpmDependencyFlags[] { RpmDependencyFlags.EQUAL, RpmDependencyFlags.LESS } );
        OP_MAP.put ( "gt", new RpmDependencyFlags[] { RpmDependencyFlags.GREATER } );
        OP_MAP.put ( "lt", new RpmDependencyFlags[] { RpmDependencyFlags.LESS } );
    }

    protected String name;

    protected String version;

    protected Set<RpmDependencyFlags> flags = new HashSet<> ();

    public void set ( final String string )
    {
        final String[] toks = string.split ( "\\s+" );
        if ( toks.length == 1 )
        {
            setAll ( toks[0], null );
        }
        else if ( toks.length == 3 )
        {
            final RpmDependencyFlags[] flags = OP_MAP.get ( toks[1] );
            if ( flags == null )
            {
                throw new IllegalArgumentException ( String.format ( "Operator '%s' is unknown", toks[1] ) );
            }
            setAll ( toks[0], toks[2], flags );
        }
        else
        {
            throw new IllegalArgumentException ( String.format ( "Invalid short format: '%s'", string ) );
        }
    }

    private void setAll ( final String name, final String version, final RpmDependencyFlags... flags )
    {
        this.name = name;
        this.version = version;
        this.flags.clear ();
        if ( flags != null )
        {
            this.flags.addAll ( Arrays.asList ( flags ) );
        }
    }

    public String getName ()
    {
        return this.name;
    }

    public String getVersion ()
    {
        return this.version;
    }

    public Set<RpmDependencyFlags> getFlags ()
    {
        return this.flags;
    }
}
