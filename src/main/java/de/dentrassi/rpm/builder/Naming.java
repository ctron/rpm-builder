/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

    private Case caseValue = Case.UNMODIFIED;

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
}
