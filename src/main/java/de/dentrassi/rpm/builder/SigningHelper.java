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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.eclipse.packager.security.pgp.PgpHelper;

public final class SigningHelper
{

    private SigningHelper ()
    {
    }

    public static PGPPrivateKey loadKey ( final Signature signature, final Logger logger ) throws MojoFailureException, MojoExecutionException
    {
        if ( signature == null )
        {
            return null;
        }

        if ( signature.isSkip () )
        {
            return null;
        }

        if ( signature.getKeyId () == null || signature.getKeyId ().isEmpty () )
        {
            throw new MojoFailureException ( signature, "'keyId' parameter not set", "Signing requires the 'keyId' to the user id of the GPG key to use." );
        }

        if ( signature.getKeyringFile () == null )
        {
            throw new MojoFailureException ( signature, "'keyringFile' parameter not set", "Signing requires the 'keyringFile' to be set to a valid GPG keyring file, containing the secret keys." );
        }

        if ( signature.getPassphrase () == null )
        {
            throw new MojoFailureException ( signature, "'passphrase' parameter not set", "Signing requires the 'passphrase' parameter to be set." );
        }

        try ( InputStream input = new FileInputStream ( signature.getKeyringFile () ) )
        {
            final PGPPrivateKey privateKey = PgpHelper.loadPrivateKey ( input, signature.getKeyId (), signature.getPassphrase () );
            if ( privateKey == null )
            {
                throw new MojoFailureException ( String.format ( "Unable to load GPG key '%s' from '%s'", signature.getKeyId (), signature.getKeyringFile () ) );
            }
            logger.info ( "Signing RPM - keyId: %016x", privateKey.getKeyID () );
            return privateKey;
        }
        catch ( final PGPException | IOException e )
        {
            throw new MojoExecutionException ( "Failed to load private key for signing", e );
        }
    }
}
