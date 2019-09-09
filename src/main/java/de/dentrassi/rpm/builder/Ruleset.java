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

import java.util.LinkedList;
import java.util.List;

public class Ruleset
{
    private String id;

    private List<Rule> rules = new LinkedList<> ();

    private String defaultRuleset;

    public void setId ( final String id )
    {
        this.id = id;
    }

    public String getId ()
    {
        return this.id;
    }

    public void setRules ( final List<Rule> rules )
    {
        this.rules = rules;
    }

    public List<Rule> getRules ()
    {
        return this.rules;
    }

    public void setDefaultRuleset ( final String defaultRuleset )
    {
        this.defaultRuleset = defaultRuleset;
    }

    public String getDefaultRuleset ()
    {
        return this.defaultRuleset;
    }

    public void validate ()
    {
        for ( final Rule rule : this.rules )
        {
            rule.validate ();
        }
    }
}
