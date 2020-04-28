package com.github.autobump.maven.model;

import com.github.autobump.core.model.Plugin;
import com.github.autobump.core.model.PluginResolver;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static junit.framework.Assert.assertEquals;

class MavenPluginResolverTest {
    private transient PluginResolver pluginResolver;
    private transient Workspace workspace;
    @BeforeEach
    void setUp() {
        pluginResolver = new MavenPluginResolver();
        workspace = new Workspace("src/test/resources/project_root_plugins");
    }

    @Test
    void resolve() {
        Set<Plugin> plugins = pluginResolver.resolve(workspace);
        assertEquals(
                Set.of(Plugin.builder()
                        .group("org.apache.derby")
                        .name("derby")
                        .version("10.15.2.0")
                        .build()),
                plugins);
    }

    @Test
    void testPluginManagement() {
        workspace = new Workspace(workspace.getProjectRoot() + "/pluginmanagement");
        Set<Plugin> plugins = pluginResolver.resolve(workspace);
        assertEquals(
                Set.of(Plugin.builder()
                        .group("org.apache.derby")
                        .name("derby")
                        .version("10.15.2.0")
                        .build()),
                plugins);
    }

    @Test
    void testNoPluginsFound() {
        workspace = new Workspace(workspace.getProjectRoot() + "/empty");
        Set<Plugin> plugins = pluginResolver.resolve(workspace);
        assertEquals(Set.of() , plugins);
    }

    @Test
    void testPluginEmptyVersion() {
        workspace = new Workspace(workspace.getProjectRoot() + "/noVersion");
        Set<Plugin> plugins = pluginResolver.resolve(workspace);
        assertEquals(Set.of(), plugins);
    }

    @Test
    void testEmptyBuild() {
        workspace = new Workspace(workspace.getProjectRoot() + "/emptyBuild");
        Set<Plugin> plugins = pluginResolver.resolve(workspace);
        assertEquals(Set.of(), plugins);
    }

    @Test
    void testEmpltyPlugins() {
        workspace = new Workspace(workspace.getProjectRoot() + "/emptyPlugins");
        Set<Plugin> plugins = pluginResolver.resolve(workspace);
        assertEquals(Set.of(), plugins);
    }

    @Test
    void testEmptyPluginManagement() {
        workspace = new Workspace(workspace.getProjectRoot() + "/emptyPluginManagement");
        Set<Plugin> plugins = pluginResolver.resolve(workspace);
        assertEquals(Set.of(), plugins);
    }

    @Test
    void pluginwithProperties() {
        workspace = new Workspace(workspace.getProjectRoot() + "/pluginsWithProperties");
        Set<Plugin> plugins = pluginResolver.resolve(workspace);
        assertEquals(
                Set.of(Plugin.builder()
                        .group("org.apache.derby")
                        .name("derby")
                        .version("10.15.2.0")
                        .build()),
                plugins);
    }

    @Test
    void pluginWithNonExestentProperties() {
        workspace = new Workspace(workspace.getProjectRoot() + "/nonExistentproperties");
        Set<Plugin> plugins = pluginResolver.resolve(workspace);
        assertEquals(
                Set.of(),
                plugins);
    }
}
