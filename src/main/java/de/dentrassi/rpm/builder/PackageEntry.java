/*******************************************************************************
 * Copyright (c) 2016 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *******************************************************************************/
package de.dentrassi.rpm.builder;

import java.io.File;

public class PackageEntry extends EntryDetails
{
    public static class Collector
    {
        private File from;

        private boolean directories = true;

        private boolean symbolicLinks = false;

        public File getFrom ()
        {
            return this.from;
        }

        public void setFrom ( final File from )
        {
            this.from = from;
        }

        public void setDirectories ( final boolean directories )
        {
            this.directories = directories;
        }

        public boolean isDirectories ()
        {
            return this.directories;
        }

        public void setSymbolicLinks ( final boolean symbolicLinks )
        {
            this.symbolicLinks = symbolicLinks;
        }

        public boolean isSymbolicLinks ()
        {
            return this.symbolicLinks;
        }

        @Override
        public String toString ()
        {
            return String.format ( "[collector - from: %s,  directories: %s, symLinks: %s]", this.from, this.directories, this.symbolicLinks );
        }
    }

    private String name;

    private Boolean directory;

    private File file;

    private Collector collect;

    private String linkTo;

    private String ruleset;

    public String getName ()
    {
        return this.name;
    }

    public void setName ( final String name )
    {
        this.name = name;
    }

    public Boolean getDirectory ()
    {
        return this.directory;
    }

    public void setDirectory ( final Boolean directory )
    {
        this.directory = directory;
    }

    public File getFile ()
    {
        return this.file;
    }

    public void setFile ( final File file )
    {
        this.file = file;
    }

    public Collector getCollect ()
    {
        return this.collect;
    }

    public void setCollect ( final Collector collect )
    {
        this.collect = collect;
    }

    public void setLinkTo ( final String linkTo )
    {
        this.linkTo = linkTo;
    }

    public String getLinkTo ()
    {
        return this.linkTo;
    }

    public void setRuleset ( final String ruleset )
    {
        this.ruleset = ruleset;
    }

    public String getRuleset ()
    {
        return this.ruleset;
    }

    @Override
    public void validate ()
    {
        if ( this.name == null || this.name.isEmpty () )
        {
            throw new IllegalStateException ( "'name' must not be empty" );
        }

        int sources = 0;

        sources += this.directory != null && this.directory ? 1 : 0;
        sources += this.file != null ? 1 : 0;
        sources += this.collect != null ? 1 : 0;
        sources += this.linkTo != null ? 1 : 0;

        if ( sources != 1 )
        {
            throw new IllegalStateException ( "Exactly one of 'file', 'directory', 'linkTo' or 'collect' must be specified." );
        }

        super.validate ();
    }
}
