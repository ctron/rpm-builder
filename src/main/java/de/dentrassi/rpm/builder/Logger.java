package de.dentrassi.rpm.builder;

import org.apache.maven.plugin.logging.Log;

public class Logger
{
    private final Log log;

    public Logger ( final Log log )
    {
        this.log = log;
    }

    public void debug ( final String format, final Object... values )
    {
        this.log.debug ( String.format ( format, values ) );
    }

    public void info ( final String format, final Object... values )
    {
        this.log.info ( String.format ( format, values ) );
    }
}
