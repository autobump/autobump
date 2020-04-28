package com.github.autobump.maven.model;

import com.github.autobump.core.model.Plugin;
import com.github.autobump.core.model.PluginResolver;
import com.github.autobump.core.model.Workspace;
import org.apache.maven.model.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MavenPluginResolver implements PluginResolver {
    private final transient MavenModelAnalyser mavenModelAnalyser;

    public MavenPluginResolver() {
        this.mavenModelAnalyser = new MavenModelAnalyser();
    }

    @Override
    public Set<Plugin> resolve(Workspace workspace) {
        Model model = mavenModelAnalyser.getModel(workspace);
        List<org.apache.maven.model.Plugin> pluginList = new ArrayList<>();
        if (model.getBuild() != null) {
            pluginList.addAll(model.getBuild().getPlugins());
            if (model.getBuild().getPluginManagement() != null) {
                pluginList.addAll(model.getBuild().getPluginManagement().getPlugins());
            }
        }
        return pluginList.stream()
                .filter(plugin -> plugin.getVersion() != null)
                .map(plugin -> Plugin.builder()
                        .group(plugin.getGroupId())
                        .name(plugin.getArtifactId())
                        .version(mavenModelAnalyser.getVersionFromProperties(model, plugin.getVersion()))
                        .build())
                .filter(plugin -> plugin.getVersion() != null)
                .collect(Collectors.toUnmodifiableSet());
    }


}
