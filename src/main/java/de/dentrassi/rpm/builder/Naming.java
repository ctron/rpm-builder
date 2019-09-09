/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package de.dentrassi.rpm.builder;

public class Naming
{
    public static enum Case
    {
        UNMODIFIED,
        LOWERCASE
    }

    public static enum DefaultFormat
    {
        DEFAULT,
        LEGACY;
    }

    private Case caseValue = Case.UNMODIFIED;

    private DefaultFormat defaultFormat = DefaultFormat.DEFAULT;

    public void setCase ( final String caseValue )
    {
        if ( caseValue != null )
        {
            this.caseValue = Case.valueOf ( caseValue.toUpperCase () );
        }
        else
        {
            this.caseValue = Case.UNMODIFIED;
        }
    }

    public Case getCase ()
    {
        return this.caseValue;
    }

    public void setDefaultFormat ( final String defaultFormat )
    {
        if ( defaultFormat != null )
        {
            this.defaultFormat = DefaultFormat.valueOf ( defaultFormat.toUpperCase () );
        }
        else
        {
            this.defaultFormat = DefaultFormat.DEFAULT;
        }
    }

    public DefaultFormat getDefaultFormat ()
    {
        return this.defaultFormat;
    }
}
