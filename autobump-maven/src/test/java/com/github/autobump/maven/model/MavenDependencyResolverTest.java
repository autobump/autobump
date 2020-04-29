package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.DependencyParserException;
import com.github.autobump.core.exceptions.NoDependencyFileFoundException;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.maven.model.testclasses.MavenDependencyResolverTester;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.InputSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UncheckedIOException;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MavenDependencyResolverTest {

    private static final  String TEST_DEPENDENCY_GROUP = "org.apache.derby";
    private static final  String TEST_DEPENDENCY_NAME = "derby";
    private static final  String TEST_DEPENDENCY_VERSION = "10.15.2.0";
    private Workspace workspace;
    private Workspace pluginWorkspace;
    private Workspace multiModuleWorkspace;
    private DependencyResolver resolver;

    @BeforeEach
    public void setUp() {
        workspace = new Workspace("src/test/resources/project_root");
        pluginWorkspace = new Workspace("src/test/resources/project_root_plugins");
        multiModuleWorkspace = new Workspace("src/test/resources/multi_module_root");
        resolver = new MavenDependencyResolver();
    }

    @Test
    public void TestSuccessresolve() {
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
    public void TestSuccessresolveProperties() {
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
                        .inputLocation(new InputLocation(25, 22 , is))
                        .build()),
                deps);
    }

    @Test
    public void TestresolveUndefinedProperty() {
        Workspace ws = new Workspace("src/test/resources/project_root_support_properties_undefinedproperty");
        Set<Dependency> deps = resolver.resolve(ws);
        assertTrue(deps.isEmpty(), "expected list of dependencies to be empty");
    }

    @Test
    public void TestresolveMalformedVarProperty() {
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
    public void TestFileNotFound() {
        assertThrows(NoDependencyFileFoundException.class, () ->
                resolver.resolve(new Workspace("src/test/resources/project_root/testDir")));
    }

    @Test
    public void TestEmpyDependencies() {
        Workspace ws = new Workspace("src/test/resources/project_root/testDir/empty");
        assertEquals(Set.of(), resolver.resolve(ws));
    }

    @Test
    public void TestUnparseableDependencies() {
        assertThrows(DependencyParserException.class, () ->
                resolver.resolve(new Workspace("src/test/resources/project_root/testDir/parserror")));
    }

    @Test
    void resolve() {
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertEquals(
                Set.of(MavenDependency.builder()
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .version(TEST_DEPENDENCY_VERSION)
                        .type(DependencyType.PLUGIN)
                        .build()),
                plugins);
    }

    @Test
    void testPluginManagement() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/pluginmanagement");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertEquals(
                Set.of(MavenDependency.builder()
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .version(TEST_DEPENDENCY_VERSION)
                        .type(DependencyType.PLUGIN)
                        .build()),
                plugins);
    }

    @Test
    void testNoPluginsFound() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/empty");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertEquals(Set.of() , plugins);
    }

    @Test
    void testPluginEmptyVersion() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/noVersion");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertEquals(Set.of(), plugins);
    }

    @Test
    void testEmptyBuild() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/emptyBuild");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertEquals(Set.of(), plugins);
    }

    @Test
    void testEmptyPlugins() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/emptyPlugins");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertEquals(Set.of(), plugins);
    }

    @Test
    void testEmptyPluginManagement() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/emptyPluginManagement");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertEquals(Set.of(), plugins);
    }

    @Test
    void pluginwithProperties() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/pluginsWithProperties");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertTrue(plugins.contains(MavenDependency.builder()
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .version(TEST_DEPENDENCY_VERSION)
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
    void testResolveMultiModuleProject() throws Exception {
        Set<Dependency> dependencies = resolver.resolve(multiModuleWorkspace);
        assertEquals(3, dependencies.size());
    }

    @Test
    void testThrowsIO() {
        MavenDependencyResolverTester tester = new MavenDependencyResolverTester();
        assertThrows(UncheckedIOException.class, () ->
                tester.resolve(multiModuleWorkspace));
    }
}
