package com.github.autobump.maven.model;

import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.Workspace;
import org.apache.maven.model.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MavenDependencyResolver implements DependencyResolver {
    private final MavenModelAnalyser mavenModelAnalyser;

    public MavenDependencyResolver() {
        this.mavenModelAnalyser = new MavenModelAnalyser();
    }

    @Override
    public Set<Dependency> resolve(Workspace workspace) {
        Model model = mavenModelAnalyser.getModel(workspace);
        Set<Dependency> dependencies = getDependencies(model);
        dependencies.addAll(getPlugins(model));
        return dependencies;
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
                        .inputLocation(plugin.getLocation("version"))
                        .version(mavenModelAnalyser.getVersionFromProperties(model, plugin.getVersion()))
                        .build())
                .filter(plugin -> plugin.getVersion() != null)
                .collect(Collectors.toSet());
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
                        .inputLocation(dependency.getLocation("version"))
                        .version(mavenModelAnalyser.getVersionFromProperties(model, dependency.getVersion()))
                        .build())
                .filter(dependency -> dependency.getVersion() != null)
                .collect(Collectors.toSet());
    }
}
