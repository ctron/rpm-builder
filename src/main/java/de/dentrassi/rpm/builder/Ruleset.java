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
