/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * Contributors:
 *     Red Hat Inc - initial API and implementation
 *******************************************************************************/
package de.dentrassi.rpm.builder.signatures;

import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.packager.rpm.build.BuilderOptions;
import org.eclipse.packager.rpm.build.RpmBuilder;

@Component(role = SignatureConfiguration.class, hint = "default")
public class DefaultSignatureConfiguration implements SignatureConfiguration {

    @Override
    public void applyOptions(final BuilderOptions options) {
    }

    @Override
    public void applyBuilder(final RpmBuilder builder) {
        builder.removeAllSignatureProcessors();
        builder.addDefaultSignatureProcessors();
    }

}
