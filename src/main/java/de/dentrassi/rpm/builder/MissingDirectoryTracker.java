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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.packager.rpm.build.BuilderContext;
import org.eclipse.packager.rpm.build.FileInformationProvider;

public class MissingDirectoryTracker implements BuilderContextListener {

    // keep track of explicit added directories
    private final Set<String> explicitAddedDirectories;
    // keep track of all missing directories which should be added after processing (without explicit added directories)
    private final Map<String, FileInformationProvider<Object>> missingDirectories;
    // base directories (prefixes) for which the intermediate directories should be generated
    private final List<String> baseDirectories;

    public MissingDirectoryTracker(List<String> baseDirectories) {
        this.explicitAddedDirectories = new HashSet<>();
        this.missingDirectories = new HashMap<>();
        this.baseDirectories = baseDirectories;
    }

    @Override
    public void notifyFileAdded(String targetName, FileInformationProvider<Object> provider) {
        addMissingDirectoriesFromPath(targetName, provider);
    }

    @Override
    public void notifyDirectoryAdded(String targetName, FileInformationProvider<Object> provider) {
        explicitAddedDirectories.add(targetName);
        missingDirectories.remove(targetName);
        addMissingDirectoriesFromPath(targetName, provider);
    }

    @Override
    public void notifySymbolicLinkAdded(String targetName, FileInformationProvider<Object> provider) {
        addMissingDirectoriesFromPath(targetName, provider);
    }

    private void addMissingDirectoriesFromPath(String targetName, FileInformationProvider<Object> provider) {
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

    private void addIfIsMissingDirectory(String intermediateDirectory, MojoFileInformationProvider mojoProvider) {
        if (startsPathWithPrefix(intermediateDirectory) && !explicitAddedDirectories.contains(intermediateDirectory)) {
            missingDirectories.computeIfAbsent(intermediateDirectory,
                    (String directory) -> new MojoFileInformationProvider(
                            mojoProvider.getRulesetEval(),
                            mojoProvider.getRuleId(),
                            null,
                            mojoProvider.getLogger(),
                            mojoProvider.getTimestamp()));
        }
    }

    private boolean containsCollectEntry(MojoFileInformationProvider mojoProvider) {
        return mojoProvider.getEntry().getCollect() != null;
    }

    private boolean startsPathWithPrefix(String directory) {
        return baseDirectories.stream().anyMatch(directory::startsWith);
    }

    public void addMissingIntermediateDirectoriesToContext(BuilderContext ctx) throws IOException {
        for (Map.Entry<String, FileInformationProvider<Object>> missingEntry : missingDirectories.entrySet()) {
            ctx.addDirectory(missingEntry.getKey(), missingEntry.getValue());
        }
    }

    private List<String> getIntermediateDirectories(String targetName) {
        ArrayList<String> intermediateDirectories = new ArrayList<>();
        Path path = Paths.get(targetName);

        for (int i = 1; i < path.getNameCount(); i++) {
            Path subPath = path.subpath(0, i);
            intermediateDirectories.add("/" + subPath);
        }

        return intermediateDirectories;
    }

    private List<String> getIntermediateDirectoriesIncludingTarget(String targetName) {
        List<String> intermediateDirectories = getIntermediateDirectories(targetName);
        intermediateDirectories.add(targetName);

        return intermediateDirectories;
    }

}
