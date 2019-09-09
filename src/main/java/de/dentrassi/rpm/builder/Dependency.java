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

import java.util.Set;

import org.eclipse.packager.rpm.deps.RpmDependencyFlags;

import com.google.common.base.Strings;

public class Dependency extends SimpleDependency
{
    public void setName ( final String name )
    {
        this.name = name;
    }

    public void setVersion ( final String version )
    {
        this.version = version;
    }

    public void setFlags ( final Set<RpmDependencyFlags> flags )
    {
        this.flags.addAll ( flags );
    }

    public void setGe ( final Object ignored )
    {
        setGreaterOrEqual ( null );
    }

    public void setGreaterOrEqual ( final Object ignored )
    {
        this.flags.add ( RpmDependencyFlags.GREATER );
        this.flags.add ( RpmDependencyFlags.EQUAL );
    }

    public void setLe ( final Object ignored )
    {
        setLessOrEqual ( null );
    }

    public void setLessOrEqual ( final Object ignored )
    {
        this.flags.add ( RpmDependencyFlags.LESS );
        this.flags.add ( RpmDependencyFlags.EQUAL );
    }

    public void setEq ( final Object ignored )
    {
        this.flags.add ( RpmDependencyFlags.EQUAL );
    }

    public void setEqual ( final Object ignored )
    {
        this.flags.add ( RpmDependencyFlags.EQUAL );
    }

    public void setGreater ( final Object ignored )
    {
        this.flags.add ( RpmDependencyFlags.GREATER );
    }

    public void setLess ( final Object ignored )
    {
        this.flags.add ( RpmDependencyFlags.LESS );
    }

    public void setPre ( final Object ignored )
    {
        this.flags.add ( RpmDependencyFlags.PREREQ );
    }

    public void validate ()
    {
        if ( Strings.isNullOrEmpty ( this.name ) )
        {
            throw new IllegalStateException ( "'name' of dependency must be set" );
        }
    }
}
