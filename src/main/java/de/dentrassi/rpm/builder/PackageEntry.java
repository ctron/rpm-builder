package de.dentrassi.rpm.builder;

public class PackageEntry extends EntryDetails
{
    public static class Collector
    {
        private String from;

        private boolean directories = true;

        public String getFrom ()
        {
            return this.from;
        }

        public void setFrom ( final String from )
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

        @Override
        public String toString ()
        {
            return String.format ( "[collector - from: %s,  directories: %s]", this.from, this.directories );
        }
    }

    private String name;

    private Boolean directory;

    private String file;

    private Collector collect;

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

    public String getFile ()
    {
        return this.file;
    }

    public void setFile ( final String file )
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

        if ( sources != 1 )
        {
            throw new IllegalStateException ( "Exactly one of 'file', 'directory' or 'collect' must be specified." );
        }

        super.validate ();
    }
}
