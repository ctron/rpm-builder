/*******************************************************************************
 * Copyright (c) 2023 Fraunhofer-Institut fuer Optronik, Systemtechnik und Bildauswertung IOSB and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * Contributors:
 *     Fraunhofer-Institut fuer Optronik, Systemtechnik und Bildauswertung IOSB - initial API and implementation
 *******************************************************************************/
package de.dentrassi.rpm.builder;

import org.eclipse.packager.rpm.build.FileInformationProvider;

public interface BuilderContextListener {

    void notifyFileAdded(String name, FileInformationProvider<Object> provider);

    void notifyDirectoryAdded(String name, FileInformationProvider<Object> provider);

    void notifySymbolicLinkAdded(String name, FileInformationProvider<Object> provider);
}
