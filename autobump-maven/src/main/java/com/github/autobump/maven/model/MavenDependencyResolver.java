package com.github.autobump.maven.model;

import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.Workspace;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;


import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MavenDependencyResolver implements DependencyResolver {
    private static final String FILENAME = "pom.xml";
    private static final String LOCATION_KEY = "version";

    private final MavenModelAnalyser mavenModelAnalyser;

    public MavenDependencyResolver() {
        this.mavenModelAnalyser = new MavenModelAnalyser();
    }

    @Override
    public Set<Dependency> resolve(Workspace workspace) {
        Model model = mavenModelAnalyser.getModel(workspace);
        Set<Dependency> dependencies = getDependencies(model);
        dependencies.addAll(getPlugins(model));
        dependencies.addAll(getParentDependency(model));
        dependencies.addAll(resolveModules(workspace, model.getModules()));
        dependencies.addAll(getDependenciesFromDependencyManagementSection(model));
        return dependencies;
    }

    private Set<Dependency> resolveModules(Workspace workspace, List<String> modules) {
        if (!modules.isEmpty()) {
            try {
                var dependencies = new HashSet<Dependency>();
                walkFiles(workspace, dependencies);
                return dependencies;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return Collections.emptySet();
    }

    public void walkFiles(Workspace workspace, Set<Dependency> dependencies) throws IOException {
        Files.walkFileTree(Path.of(workspace.getProjectRoot()),
                Set.of(),
                2,
                new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (!file.toString().equals(workspace.getProjectRoot() + File.separator + FILENAME) &&
                                file.getFileName().toString().equals(FILENAME)) {
                            Workspace ws = new Workspace(file
                                    .toAbsolutePath()
                                    .toString()
                                    .replace(File.separator + FILENAME, ""));
                            dependencies.addAll(resolve(ws));
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
    }

    private Set<Dependency> getPlugins(Model model) {
        List<org.apache.maven.model.Plugin> pluginList = new ArrayList<>();
        if (model.getBuild() != null) {
            pluginList.addAll(model.getBuild().getPlugins());
            if (model.getBuild().getPluginManagement() != null) {
                pluginList.addAll(model.getBuild().getPluginManagement().getPlugins());
            }
        }
        return pluginList.stream()
                .filter(plugin -> plugin.getVersion() != null)
                .map(plugin -> MavenDependency.builder()
                        .group(plugin.getGroupId())
                        .name(plugin.getArtifactId())
                        .type(DependencyType.PLUGIN)
                        .inputLocation(plugin.getLocation(LOCATION_KEY))
                        .version(mavenModelAnalyser.getVersionFromProperties(model, plugin.getVersion()))
                        .build())
                .filter(plugin -> plugin.getVersion() != null)
                .collect(Collectors.toSet());
    }


    private Set<Dependency> getDependenciesFromDependencyManagementSection(Model model) {
        DependencyManagement mng = model.getDependencyManagement();
        if (mng != null) {
            return mng
                    .getDependencies()
                    .stream()
                    .filter(dep -> dep.getVersion() != null)
                    .map(dep -> MavenDependency.builder()
                            .group(dep.getGroupId())
                            .name(dep.getArtifactId())
                            .version(dep.getVersion())
                            .type(DependencyType.DEPENDENCY)
                            .inputLocation(dep.getLocation(LOCATION_KEY))
                            .build())
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private Set<Dependency> getParentDependency(Model model){
        var parent = model.getParent();
        Set<Dependency> dependencies = new HashSet<>();
        if (parent != null) {
            dependencies.add(MavenDependency
                    .builder()
                    .inputLocation(parent.getLocation(LOCATION_KEY))
                    .group(parent.getGroupId())
                    .name(parent.getArtifactId())
                    .version(parent.getVersion())
                    .type(DependencyType.PARENT_DEPENDENCY)
                    .build());
        }
        return dependencies;
    }

    private Set<Dependency> getDependencies(Model model) {
        return model
                .getDependencies()
                .stream()
                .filter(dependency -> dependency.getVersion() != null)
                .map(dependency -> MavenDependency.builder()
                        .group(dependency.getGroupId())
                        .name(dependency.getArtifactId())
                        .type(DependencyType.DEPENDENCY)
                        .inputLocation(dependency.getLocation(LOCATION_KEY))
                        .version(mavenModelAnalyser.getVersionFromProperties(model, dependency.getVersion()))
                        .build())
                .filter(dependency -> dependency.getVersion() != null)
                .collect(Collectors.toSet());
    }
}
