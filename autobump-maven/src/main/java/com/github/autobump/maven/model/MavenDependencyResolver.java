package com.github.autobump.maven.model;

import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.Workspace;
import org.apache.maven.model.BuildBase;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Profile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MavenDependencyResolver implements DependencyResolver {
    private static final String LOCATION_KEY = "version";

    private final MavenModelAnalyser mavenModelAnalyser;

    public MavenDependencyResolver() {
        this.mavenModelAnalyser = new MavenModelAnalyser();
    }

    @Override
    public Set<Dependency> resolve(Workspace workspace) {
        return resolve(workspace, new HashSet<>());
    }

    @Override
    public Set<Dependency> resolve(Workspace workspace, Set<Dependency> ignoredInternal) {
        Model model = mavenModelAnalyser.getModel(workspace);
        ignoredInternal.add(Dependency
                .builder()
                .name(model.getArtifactId())
                .group(model.getGroupId())
                .version(new MavenVersion(model.getVersion()))
                .build());
        Set<Dependency> dependencies = getDependencies(workspace, ignoredInternal, model);
        return dependencies.stream().filter(dependency ->
                !ignoredInternal.contains(Dependency.builder()
                        .group(dependency.getGroup())
                        .version(dependency.getVersion())
                        .name(dependency.getName())
                        .build()))
                .filter(dependency ->
                        !dependency.getGroup().equals("${project.groupId}"))
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<Dependency> getDependencies(Workspace workspace, Set<Dependency> ignoredInternal, Model model) {
        Set<Dependency> dependencies = getDependencySet(model);
        dependencies.addAll(getPlugins(model));
        dependencies.addAll(getParentDependency(model));
        dependencies.addAll(resolveModules(workspace, model.getModules(), ignoredInternal));
        dependencies.addAll(getProfiles(model));
        dependencies.addAll(getDependenciesFromDependencyManagementSection(model));
        return dependencies;
    }

    private Set<Dependency> getProfiles(Model model) {
        Set<Dependency> dependencies = new HashSet<>();
        for (Profile profile : model.getProfiles()) {
            dependencies.addAll(
                    getPluginsFromProfile(profile, model)
            );

            dependencies.addAll(
                    getDependenciesFromProfile(model, profile)
            );

            dependencies.addAll(
                    getDependencyManageMentFromProfile(model, profile)
            );
        }
        return dependencies;
    }

    private Set<Dependency> getDependencyManageMentFromProfile(Model model, Profile profile) {
        if (profile.getDependencyManagement() != null) {
            return profile.getDependencyManagement()
                    .getDependencies()
                    .stream()
                    .map(dependency -> MavenDependency.builder()
                            .group(dependency.getGroupId())
                            .name(dependency.getArtifactId())
                            .type(DependencyType.PROFILE_DEPENDENCY)
                            .inputLocation(dependency.getLocation(LOCATION_KEY))
                            .version(new MavenVersion(mavenModelAnalyser
                                    .getVersionFromProperties(model, dependency.getVersion(), profile)))
                            .build())
                    .filter(dependency -> dependency.getVersion().getVersionNumber() != null)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private Set<Dependency> getDependenciesFromProfile(Model model, Profile profile) {

        return profile.getDependencies()
                .stream()
                .map(dependency -> MavenDependency.builder()
                        .group(dependency.getGroupId())
                        .name(dependency.getArtifactId())
                        .type(DependencyType.PROFILE_DEPENDENCY)
                        .inputLocation(dependency.getLocation(LOCATION_KEY))
                        .version(new MavenVersion(mavenModelAnalyser
                                .getVersionFromProperties(model, dependency.getVersion(), profile)))
                        .build())
                .filter(dependency -> dependency.getVersion().getVersionNumber() != null)
                .collect(Collectors.toSet());
    }

    private Set<Dependency> getPluginsFromProfile(Profile profile, Model model) {
        return resolvePlugins(profile.getBuild())
                .stream()
                .map(plugin -> MavenDependency.builder()
                        .group(plugin.getGroupId())
                        .name(plugin.getArtifactId())
                        .type(DependencyType.PROFILE_PLUGIN)
                        .inputLocation(plugin.getLocation(LOCATION_KEY))
                        .version(new MavenVersion(mavenModelAnalyser
                                .getVersionFromProperties(model, plugin.getVersion(), profile)))
                        .build())
                .filter(plugin -> plugin.getVersion().getVersionNumber() != null)
                .collect(Collectors.toSet());
    }

    Set<Dependency> resolveModules(Workspace workspace, List<String> modules, Set<Dependency> toBeIgnored) {
        if (!modules.isEmpty()) {
            var dependencies = new HashSet<Dependency>();
            for (String module : modules) {
                dependencies.addAll(resolve(
                        new Workspace(workspace.getProjectRoot() + File.separator + module), toBeIgnored));
            }
            return dependencies;
        }
        return Collections.emptySet();
    }

    private Set<Dependency> getPlugins(Model model) {
        List<Plugin> pluginList = resolvePlugins(model.getBuild());
        return pluginList.stream()
                .map(plugin -> MavenDependency.builder()
                        .group(plugin.getGroupId())
                        .name(plugin.getArtifactId())
                        .type(DependencyType.PLUGIN)
                        .inputLocation(plugin.getLocation(LOCATION_KEY))
                        .version(new MavenVersion(mavenModelAnalyser
                                .getVersionFromProperties(model, plugin.getVersion())))
                        .build())
                .filter(plugin -> plugin.getVersion().getVersionNumber() != null)
                .collect(Collectors.toSet());
    }

    private Set<Dependency> getDependenciesFromDependencyManagementSection(Model model) {
        DependencyManagement mng = model.getDependencyManagement();
        if (mng != null) {
            return mng
                    .getDependencies()
                    .stream()
                    .map(dep -> MavenDependency.builder()
                            .group(dep.getGroupId())
                            .name(dep.getArtifactId())
                            .version(new MavenVersion(mavenModelAnalyser
                                    .getVersionFromProperties(model, dep.getVersion())))
                            .type(DependencyType.DEPENDENCY)
                            .inputLocation(dep.getLocation(LOCATION_KEY))
                            .build())
                    .filter(dependency -> dependency.getVersion().getVersionNumber() != null)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

    private Set<Dependency> getParentDependency(Model model) {
        var parent = model.getParent();
        Set<Dependency> dependencies = new HashSet<>();
        if (parent != null) {
            dependencies.add(MavenDependency
                    .builder()
                    .inputLocation(parent.getLocation(LOCATION_KEY))
                    .group(parent.getGroupId())
                    .name(parent.getArtifactId())
                    .version(new MavenVersion(parent.getVersion()))
                    .type(DependencyType.PARENT_DEPENDENCY)
                    .build());
        }
        return dependencies;
    }

    private List<Plugin> resolvePlugins(BuildBase build) {
        List<Plugin> pluginList = new ArrayList<>();
        if (build != null) {
            pluginList.addAll(build.getPlugins());
            if (build.getPluginManagement() != null) {
                pluginList.addAll(build.getPluginManagement().getPlugins());
            }
        }
        return pluginList;
    }

    private Set<Dependency> getDependencySet(Model model) {
        List<org.apache.maven.model.Dependency> dependencies = model.getDependencies();
        return dependencies
                .stream()
                .map(dependency -> MavenDependency.builder()
                        .group(dependency.getGroupId())
                        .name(dependency.getArtifactId())
                        .type(DependencyType.DEPENDENCY)
                        .inputLocation(dependency.getLocation(LOCATION_KEY))
                        .version(new MavenVersion(mavenModelAnalyser
                                .getVersionFromProperties(model, dependency.getVersion())))
                        .build())
                .filter(dependency -> dependency.getVersion().getVersionNumber() != null)
                .collect(Collectors.toSet());
    }
}
