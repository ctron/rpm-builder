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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.packager.rpm.build.BuilderContext;
import org.eclipse.packager.rpm.build.FileInformationProvider;

public class ListenableBuilderContext implements BuilderContext {

    private final BuilderContext builderContext;
    private final Set<BuilderContextListener> listeners;

    public ListenableBuilderContext(BuilderContext builderContext) {
        this.builderContext = builderContext;
        this.listeners = new HashSet<>();
    }

    @Override
    public void setDefaultInformationProvider(FileInformationProvider<Object> provider) {
        builderContext.setDefaultInformationProvider(provider);
    }

    @Override
    public FileInformationProvider<Object> getDefaultInformationProvider() {
        return builderContext.getDefaultInformationProvider();
    }

    @Override
    public void addFile(String targetName, Path source, FileInformationProvider<? super Path> provider) throws IOException {
        builderContext.addFile(targetName, source, provider);
        listeners.forEach(listener -> listener.notifyFileAdded(targetName, (MojoFileInformationProvider) provider));
    }

    @Override
    public void addFile(String targetName, InputStream source, FileInformationProvider<Object> provider) throws IOException {
        builderContext.addFile(targetName, source, provider);
        listeners.forEach(listener -> listener.notifyFileAdded(targetName, provider));
    }

    @Override
    public void addFile(String targetName, ByteBuffer source, FileInformationProvider<Object> provider) throws IOException {
        builderContext.addFile(targetName, source, provider);
        listeners.forEach(listener -> listener.notifyFileAdded(targetName, provider));
    }

    @Override
    public void addDirectory(String targetName, FileInformationProvider<? super Directory> provider) throws IOException {
        builderContext.addDirectory(targetName, provider);
        listeners.forEach(listener -> listener.notifyDirectoryAdded(targetName, (MojoFileInformationProvider) provider));
    }

    @Override
    public void addSymbolicLink(String targetName, String linkTo, FileInformationProvider<? super SymbolicLink> provider) throws IOException {
        builderContext.addSymbolicLink(targetName, linkTo, provider);
        listeners.forEach(listener -> listener.notifySymbolicLinkAdded(targetName, (MojoFileInformationProvider) provider));
    }

    public void registerListener(BuilderContextListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(BuilderContextListener listener) {
        listeners.remove(listener);
    }

}
