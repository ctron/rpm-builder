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

import java.io.File;

public class Signature
{
    private String keyId;

    private File keyringFile;

    private String passphrase;

    private String hashAlgorithm = "SHA512";

    private boolean skip;

    public void setKeyId ( final String keyId )
    {
        this.keyId = keyId;
    }

    public String getKeyId ()
    {
        return this.keyId;
    }

    public void setKeyringFile ( final File keyringFile )
    {
        this.keyringFile = keyringFile;
    }

    public File getKeyringFile ()
    {
        return this.keyringFile;
    }

    public void setPassphrase ( final String passphrase )
    {
        this.passphrase = passphrase;
    }

    public String getPassphrase ()
    {
        return this.passphrase;
    }

    public void setHashAlgorithm ( final String hashAlgorithm )
    {
        this.hashAlgorithm = hashAlgorithm;
    }

    public String getHashAlgorithm ()
    {
        return this.hashAlgorithm;
    }

    public void setSkip ( final boolean skip )
    {
        this.skip = skip;
    }

    public boolean isSkip ()
    {
        return this.skip;
    }
}
