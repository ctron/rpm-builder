package de.dentrassi.rpm.builder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.packagedrone.utils.rpm.build.FileInformation;
import org.eclipse.packagedrone.utils.rpm.build.PayloadEntryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RulesetEvaluator
{
    private static final Logger logger = LoggerFactory.getLogger ( RulesetEvaluator.class );

    private class RulesetInstance
    {
        private final String id;

        private final List<Rule> rules;

        private final String parentRuleset;

        public RulesetInstance ( final String id, final List<Rule> rules, final String parentRuleset )
        {
            this.id = id;
            this.rules = rules;
            this.parentRuleset = parentRuleset;
        }

        public void eval ( final Object object, final PayloadEntryType type, final String targetName, final FileInformation info )
        {
            for ( final Rule rule : this.rules )
            {
                logger.debug ( "Testing rule {}", rule );

                if ( rule.matches ( object, type, targetName ) )
                {
                    logger.debug ( "  Rule matches. Applying information ..." );
                    if ( rule.apply ( info ) )
                    {
                        logger.debug ( "    Information: {}", info );
                    }
                    if ( rule.isLast () )
                    {
                        logger.debug ( "  Last rule" );
                        return;
                    }
                }
            }

            if ( this.parentRuleset != null && !this.parentRuleset.isEmpty () )
            {
                logger.debug ( "Running parent ruleset: '{}'", this.parentRuleset );
                RulesetEvaluator.this.eval ( this.parentRuleset, object, type, targetName, info );
            }
        }
    }

    private final Map<String, RulesetInstance> rulesets = new LinkedHashMap<> ();

    public RulesetEvaluator ( final Collection<Ruleset> rulesets )
    {
        this ( null, rulesets );
    }

    public RulesetEvaluator ( final RulesetEvaluator parent, final Collection<Ruleset> rulesets )
    {
        for ( final Ruleset set : rulesets )
        {
            this.rulesets.put ( set.getId (), convert ( set ) );
        }
    }

    private RulesetInstance convert ( final Ruleset set )
    {
        set.validate ();

        return new RulesetInstance ( set.getId (), set.getRules (), set.getDefaultRuleset () );
    }

    public void eval ( final String ruleId, final Object object, final PayloadEntryType type, final String targetName, final FileInformation info )
    {
        final RulesetInstance ruleset = this.rulesets.get ( ruleId );

        if ( ruleset == null )
        {
            throw new IllegalStateException ( String.format ( "Unknown rule: '%s'", ruleId ) );
        }

        ruleset.eval ( object, type, targetName, info );
    }
}
