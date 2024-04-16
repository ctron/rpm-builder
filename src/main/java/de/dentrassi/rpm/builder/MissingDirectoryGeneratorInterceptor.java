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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.packager.rpm.build.BuilderContext;
import org.eclipse.packager.rpm.build.FileInformationProvider;

public class MissingDirectoryGeneratorInterceptor implements BuilderContext {

    private final BuilderContext builderContext;

    // base directories (prefixes) for which the intermediate directories should be generated
    private final List<String> baseDirectories;
    // keep track of explicit added directories
    private final Set<String> explicitAddedDirectories;
    // keep track of all additionally generated and added directories
    private final Set<String> generatedDirectories;

    public MissingDirectoryGeneratorInterceptor(BuilderContext builderContext, List<String> baseDirectories) {
        this.builderContext = builderContext;
        this.baseDirectories = baseDirectories;
        explicitAddedDirectories = new HashSet<>();
        generatedDirectories = new HashSet<>();
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
        addMissingDirectoriesFromPath(targetName, provider);
        builderContext.addFile(targetName, source, provider);
    }

    @Override
    public void addFile(String targetName, InputStream source, FileInformationProvider<Object> provider) throws IOException {
        addMissingDirectoriesFromPath(targetName, provider);
        builderContext.addFile(targetName, source, provider);
    }

    @Override
    public void addFile(String targetName, ByteBuffer source, FileInformationProvider<Object> provider) throws IOException {
        addMissingDirectoriesFromPath(targetName, provider);
        builderContext.addFile(targetName, source, provider);
    }

    @Override
    public void addDirectory(String targetName, FileInformationProvider<? super Directory> provider) throws IOException {
        addMissingDirectoriesFromPath(targetName, provider);
        builderContext.addDirectory(targetName, provider);
        explicitAddedDirectories.add(targetName);
    }

    @Override
    public void addSymbolicLink(String targetName, String linkTo, FileInformationProvider<? super SymbolicLink> provider) throws IOException {
        addMissingDirectoriesFromPath(targetName, provider);
        builderContext.addSymbolicLink(targetName, linkTo, provider);
    }

    private void addMissingDirectoriesFromPath(String targetName, FileInformationProvider<?> provider) throws IOException {
        if (provider instanceof MojoFileInformationProvider) {
            MojoFileInformationProvider mojoProvider = (MojoFileInformationProvider) provider;

            List<String> intermediateDirectories;
            if (containsCollectEntry(mojoProvider)) {
                // collect handles intermediate directories by themselves via flag 'directories'
                // -> only use base directory (entry name of collect) as target path to calculate
                // intermediate directories
                targetName = mojoProvider.getEntry().getName();
                intermediateDirectories = getIntermediateDirectoriesIncludingTarget(targetName);
            } else {
                intermediateDirectories = getIntermediateDirectories(targetName);
            }

            for (String intermediateDirectory : intermediateDirectories) {
                addIfIsMissingDirectory(intermediateDirectory, mojoProvider);
            }
        }
    }

    private void addIfIsMissingDirectory(String intermediateDirectory, MojoFileInformationProvider mojoProvider) throws IOException {
        if (startsPathWithPrefix(intermediateDirectory)
                && !explicitAddedDirectories.contains(intermediateDirectory)
                && !generatedDirectories.contains(intermediateDirectory)) {
            builderContext.addDirectory(intermediateDirectory, new MojoFileInformationProvider(
                            mojoProvider.getRulesetEval(),
                            mojoProvider.getRuleId(),
                            null,
                            mojoProvider.getLogger(),
                            mojoProvider.getTimestamp()));
            generatedDirectories.add(intermediateDirectory);
        }
    }

    private boolean containsCollectEntry(MojoFileInformationProvider mojoProvider) {
        return mojoProvider.getEntry().getCollect() != null;
    }

    private boolean startsPathWithPrefix(String directory) {
        return baseDirectories.stream().anyMatch(directory::startsWith);
    }

    private List<String> getIntermediateDirectories(String targetName) {
        ArrayList<String> intermediateDirectories = new ArrayList<>();
        Path path = Paths.get(targetName);

        for (int i = 1; i < path.getNameCount(); i++) {
            Path subPath = path.subpath(0, i);
            intermediateDirectories.add("/" + subPath.toString().replace(File.separatorChar, '/'));
        }

        return intermediateDirectories;
    }

    private List<String> getIntermediateDirectoriesIncludingTarget(String targetName) {
        List<String> intermediateDirectories = getIntermediateDirectories(targetName);
        intermediateDirectories.add(targetName);

        return intermediateDirectories;
    }

}
