/*******************************************************************************
 * Copyright (c) 2016, 2023 IBH SYSTEMS GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * Contributors:
 *     IBH SYSTEMS GmbH - initial API and implementation
 *     Red Hat, Inc - use pgpainless
 *******************************************************************************/
package de.dentrassi.rpm.builder;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.pgpainless.key.protection.SecretKeyRingProtector;
import org.pgpainless.util.Passphrase;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class SigningHelper {

    private SigningHelper() {
    }

    public static PGPSecretKeyRing loadKey(final Signature signature, final Logger logger) throws MojoFailureException, MojoExecutionException {
        if (signature == null) {
            return null;
        }

        if (signature.isSkip()) {
            return null;
        }

        if (signature.getKeyId() == null || signature.getKeyId().isEmpty()) {
            throw new MojoFailureException(signature, "'keyId' parameter not set", "Signing requires the 'keyId' to the user id of the GPG key to use.");
        }

        if (signature.getKeyringFile() == null) {
            throw new MojoFailureException(signature, "'keyringFile' parameter not set", "Signing requires the 'keyringFile' to be set to a valid GPG keyring file, containing the secret keys.");
        }

        try (InputStream input = Files.newInputStream(signature.getKeyringFile().toPath())) {
            final long keyIdNum = Long.parseLong(signature.getKeyId(), 16);
            final PGPSecretKeyRing secretKey = new PGPSecretKeyRingCollection(input, new BcKeyFingerprintCalculator())
                    .getSecretKeyRing(keyIdNum);
            if (secretKey == null) {
                throw new MojoFailureException(String.format("Unable to load GPG key '%s' from '%s'", signature.getKeyId(), signature.getKeyringFile()));
            }
            logger.info("Signing RPM - keyId: %016x", keyIdNum);
            return secretKey;
        } catch (final PGPException | IOException e) {
            throw new MojoExecutionException("Failed to load private key for signing", e);
        }
    }

    public static SecretKeyRingProtector createProtector(PGPSecretKeyRing secretKey, String passphrase) {
        if (passphrase == null || passphrase.isEmpty()) {
            return SecretKeyRingProtector.unprotectedKeys();
        } else {
            return SecretKeyRingProtector.unlockAnyKeyWith(Passphrase.fromPassword(passphrase));
        }
    }
}
