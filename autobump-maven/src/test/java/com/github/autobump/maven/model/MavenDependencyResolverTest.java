package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.DependencyParserException;
import com.github.autobump.core.exceptions.NoDependencyFileFoundException;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.Workspace;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.InputSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MavenDependencyResolverTest {

    public static final String TEST_DEPENDENCY_GROUP = "org.apache.derby";
    public static final String TEST_DEPENDENCY_NAME = "derby";
    public static final String TEST_DEPENDENCY_VERSION = "10.15.2.0";
    public static final String TEST_PLUGIN_GROUP = "org.apache.maven.plugins";
    public static final String TEST_PLUGIN_NAME = "maven-clean-plugin";
    public static final String TEST_PLUGIN_VERSION = "2.2";
    private Workspace workspace;
    private Workspace pluginWorkspace;
    private Workspace pluginDefaultGroupIdWorkspace;
    private Workspace multiModuleWorkspace;
    private Workspace parentDependencyWorkspace;
    private DependencyResolver resolver;

    @BeforeEach
    void setUp() {
        workspace = new Workspace("src/test/resources/project_root");
        pluginWorkspace = new Workspace("src/test/resources/project_root_plugins");
        pluginDefaultGroupIdWorkspace = new Workspace("src/test/resources/project_root_plugins/pluginsDefaultGroupId");
        multiModuleWorkspace = new Workspace("src/test/resources/multi_module_root");
        parentDependencyWorkspace = new Workspace("src/test/resources/parent_dependency_root");
        resolver = new MavenDependencyResolver();
    }

    @Test
    void TestSuccessfullyResolveDependencies_SingleModuleMavenProject() {
        Set<Dependency> deps = resolver.resolve(workspace);
        InputSource is = new InputSource();
        is.setLocation("src/test/resources/project_root/pom.xml");
        assertEquals(
                Set.of(MavenDependency.builder()
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .version(TEST_DEPENDENCY_VERSION)
                        .type(DependencyType.DEPENDENCY)
                        .inputLocation(new InputLocation(21, 22, is))
                        .build()),
                deps);
    }

    @Test
    void TestSuccessfullyResolveDependencies_WithVersionInProperties() {
        Workspace ws = new Workspace("src/test/resources/project_root_support_properties");
        Set<Dependency> deps = resolver.resolve(ws);
        InputSource is = new InputSource();
        is.setLocation("src/test/resources/project_root_support_properties/pom.xml");
        assertEquals(
                Set.of(MavenDependency.builder()
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .version(TEST_DEPENDENCY_VERSION)
                        .type(DependencyType.DEPENDENCY)
                        .inputLocation(new InputLocation(25, 22, is))
                        .build()),
                deps);
    }

    @Test
    void TestResolveDependencyWithUndefinedProperty_NoDependencyRetained() {
        Workspace ws = new Workspace("src/test/resources/project_root_support_properties_undefinedproperty");
        Set<Dependency> deps = resolver.resolve(ws);
        assertTrue(deps.isEmpty(), "expected list of dependencies to be empty");
    }

    @Test
    void TestResolveMalformedVarProperty_RetainedAsDependency() {
        Workspace ws = new Workspace("src/test/resources/project_root_support_properties_badproperty");
        Set<Dependency> deps = resolver.resolve(ws);
        InputSource is = new InputSource();
        is.setLocation("src/test/resources/project_root_support_properties_badproperty/pom.xml");
        assertEquals(
                Set.of(MavenDependency.builder()
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .version("${org.apache.derby.version")
                        .type(DependencyType.DEPENDENCY)
                        .inputLocation(new InputLocation(21, 22, is))
                        .build()),
                deps);
    }

    @Test
    void TestFileNotFound_ThrowsNoDependencyFileFoundException() {
        assertThrows(NoDependencyFileFoundException.class, () ->
                resolver.resolve(new Workspace("src/test/resources/project_root/testDir")));
    }

    @Test
    void TestEmptyDependencies() {
        Workspace ws = new Workspace("src/test/resources/project_root/testDir/empty");
        assertEquals(Set.of(), resolver.resolve(ws));
    }

    @Test
    void TestUnparseableDependencies() {
        assertThrows(DependencyParserException.class, () ->
                resolver.resolve(new Workspace("src/test/resources/project_root/testDir/parserror")));
    }

    @Test
    void resolve() {
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertEquals(
                Set.of(MavenDependency.builder()
                        .group(TEST_PLUGIN_GROUP)
                        .name(TEST_PLUGIN_NAME)
                        .version(TEST_PLUGIN_VERSION)
                        .type(DependencyType.PLUGIN)
                        .build()),
                plugins);
    }

    @Test
    void testSuccessfullyResolvePluginsInPluginManagementSection() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/pluginmanagement");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertEquals(
                Set.of(MavenDependency.builder()
                        .group(TEST_PLUGIN_GROUP)
                        .name(TEST_PLUGIN_NAME)
                        .version(TEST_PLUGIN_VERSION)
                        .type(DependencyType.PLUGIN)
                        .build()),
                plugins);
    }

    @Test
    void testNoPluginsFound_ShouldReturnEmptySet() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/empty");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertEquals(Set.of(), plugins);
    }

    @Test
    void testPluginEmptyVersion_ShouldReturnEmptySet() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/noVersion");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertEquals(Set.of(), plugins);
    }

    @Test
    void getPluginsWithDefaultGroupId_ShouldSuccessfullyResolve(){
        Set<Dependency> plugins = resolver.resolve(pluginDefaultGroupIdWorkspace);

        assertTrue(plugins.contains(MavenDependency.builder()
                .group("org.apache.maven.plugins")
                .name("maven-compiler-plugin")
                .version("3.8.1")
                .type(DependencyType.PLUGIN)
                .build()));
    }

    @Test
    void testEmptyBuild_ShouldReturnEmptySet() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/emptyBuild");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertEquals(Set.of(), plugins);
    }

    @Test
    void testEmptyPlugins_ShouldReturnEmptySet() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/emptyPlugins");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertEquals(Set.of(), plugins);
    }

    @Test
    void testEmptyPluginManagement_ShouldReturnEmptySet() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/emptyPluginManagement");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertEquals(Set.of(), plugins);
    }

    @Test
    void testSuccessfullyResolvePluginwithVersionInProperties() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/pluginsWithProperties");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertTrue(plugins.contains(MavenDependency.builder()
                .group(TEST_PLUGIN_GROUP)
                .name(TEST_PLUGIN_NAME)
                .version(TEST_PLUGIN_VERSION)
                .type(DependencyType.PLUGIN)
                .build()));
    }

    @Test
    void pluginWithNonExistentProperties() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/nonExistentproperties");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertEquals(
                Set.of(),
                plugins);
    }

    @Test
    void testResolveMultiModuleProject() {
        Set<Dependency> dependencies = resolver.resolve(multiModuleWorkspace);
        assertEquals(4, dependencies.size());
    }

    @Test
    void testResolveMultiModuleProject_withDependencyManagementSection() {
        Workspace ws = new Workspace("src/test/resources/multi_module_root_depmngt");
        Set<Dependency> dependencies = resolver.resolve(ws);
        assertEquals(5, dependencies.size());
    }

    @Test
    void testIOException_shouldThrowNewUncheckedIOException() {

        class MavenDependencyResolverTester extends MavenDependencyResolver {
            @Override
            public void walkFiles(Workspace workspace, Set<Dependency> dependencies) throws IOException {
                throw new IOException();
            }
        }

        MavenDependencyResolverTester tester = new MavenDependencyResolverTester();
        assertThrows(UncheckedIOException.class, () ->
                tester.resolve(multiModuleWorkspace));
    }

    @Test
    void testSuccessfullyResolveParentDependency(){
        assertTrue(resolver.resolve(parentDependencyWorkspace)
                .contains(
                        MavenDependency
                                .builder()
                                .name("spring-boot-starter-parent")
                                .group("org.springframework.boot")
                                .version("2.2.5.RELEASE")
                                .type(DependencyType.PARENT_DEPENDENCY)
                                .build()));
    }
}
