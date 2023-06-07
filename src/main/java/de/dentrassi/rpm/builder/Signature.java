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

import java.io.File;
import java.nio.file.Path;

public class Signature {
    private String keyId;

    private File keyringFile;

    private String passphrase;

    private boolean skip;

    public void setKeyId(final String keyId) {
        this.keyId = keyId;
    }

    public String getKeyId() {
        return this.keyId;
    }

    public void setKeyringFile(final File keyringFile) {
        this.keyringFile = keyringFile;
    }

    public File getKeyringFile() {
        return this.keyringFile;
    }

    public void setPassphrase(final String passphrase) {
        this.passphrase = passphrase;
    }

    public String getPassphrase() {
        return this.passphrase;
    }

    public void setSkip(final boolean skip) {
        this.skip = skip;
    }

    public boolean isSkip() {
        return this.skip;
    }
}
