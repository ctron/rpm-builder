/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Red Hat Inc - upgrade to package drone 0.14.0
 *******************************************************************************/
package de.dentrassi.rpm.builder;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;

import org.eclipse.packager.rpm.build.BuilderContext;
import org.eclipse.packager.rpm.build.FileInformation;
import org.eclipse.packager.rpm.build.FileInformationProvider;
import org.eclipse.packager.rpm.build.PayloadEntryType;

public class MojoFileInformationProvider implements FileInformationProvider<Object>
{
    private final RulesetEvaluator rulesetEval;

    private final Consumer<String> logger;

    private final String ruleId;

    private final PackageEntry entry;

    public MojoFileInformationProvider ( final RulesetEvaluator rulesetEval, final String ruleId, final PackageEntry entry, final Consumer<String> logger )
    {
        this.rulesetEval = Objects.requireNonNull ( rulesetEval );
        this.ruleId = ruleId;
        this.entry = entry;
        this.logger = logger != null ? logger : ( s ) -> {
        };
    }

    @Override
    public FileInformation provide ( final String targetName, final Object object, final PayloadEntryType type ) throws IOException
    {
        final FileInformation result = provideByRule ( targetName, object, type );

        if ( result == null )
        {
            throw new IllegalStateException ( "Unable to provide file information" );
        }

        if ( this.entry != null )
        {
            if ( this.entry.apply ( result ) )
            {
                this.logger.accept ( String.format ( "local override = %s", result ) );
            }
        }

        return result;
    }

    private FileInformation provideByRule ( final String targetName, final Object object, final PayloadEntryType type ) throws IOException
    {
        final FileInformation result = BuilderContext.defaultProvider ().provide ( targetName, object, type );

        if ( this.ruleId != null && !this.ruleId.isEmpty () )
        {
            this.logger.accept ( String.format ( "run ruleset: '%s'", this.ruleId ) );
            this.rulesetEval.eval ( this.ruleId, object, type, targetName, result );
        }

        this.logger.accept ( String.format ( "fileInformation = %s", result ) );

        return result;
    }
}
