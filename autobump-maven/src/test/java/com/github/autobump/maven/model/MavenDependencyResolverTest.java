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

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


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
    private Workspace parentDependencyWorkspace;
    private DependencyResolver resolver;

    @BeforeEach
    void setUp() {
        workspace = new Workspace("src/test/resources/project_root");
        pluginWorkspace = new Workspace("src/test/resources/project_root_plugins");
        pluginDefaultGroupIdWorkspace = new Workspace("src/test/resources/project_root_plugins/pluginsDefaultGroupId");
        parentDependencyWorkspace = new Workspace("src/test/resources/parent_dependency_root");
        resolver = new MavenDependencyResolver();
    }

    @Test
    void TestSuccessfullyResolveDependencies_SingleModuleMavenProject() {
        Set<Dependency> deps = resolver.resolve(workspace);
        InputSource is = new InputSource();
        is.setLocation("src/test/resources/project_root/pom.xml");
        assertThat(deps).contains(MavenDependency.builder()
                .group(TEST_DEPENDENCY_GROUP)
                .name(TEST_DEPENDENCY_NAME)
                .version(TEST_DEPENDENCY_VERSION)
                .type(DependencyType.DEPENDENCY)
                .inputLocation(new InputLocation(21, 22, is))
                .build());
    }

    @Test
    void TestSuccessfullyResolveDependencies_WithVersionInProperties() {
        Workspace ws = new Workspace("src/test/resources/project_root_support_properties");
        Set<Dependency> deps = resolver.resolve(ws);
        InputSource is = new InputSource();
        is.setLocation("src/test/resources/project_root_support_properties/pom.xml");
        assertThat(deps).contains(MavenDependency.builder()
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .version(TEST_DEPENDENCY_VERSION)
                        .type(DependencyType.DEPENDENCY)
                        .inputLocation(new InputLocation(25, 22, is))
                        .build());
    }

    @Test
    void TestResolveDependencyWithUndefinedProperty_NoDependencyRetained() {
        Workspace ws = new Workspace("src/test/resources/project_root_support_properties_undefinedproperty");
        Set<Dependency> deps = resolver.resolve(ws);
        assertThat(deps).isEmpty();
    }

    @Test
    void TestResolveMalformedVarProperty_RetainedAsDependency() {
        Workspace ws =
                new Workspace("src/test/resources/project_root_support_properties_badproperty");
        Set<Dependency> deps = resolver.resolve(ws);
        InputSource is = new InputSource();
        is.setLocation("src/test/resources/project_root_support_properties_badproperty/pom.xml");

        assertThat(deps).contains(MavenDependency.builder()
                .group(TEST_DEPENDENCY_GROUP)
                .name(TEST_DEPENDENCY_NAME)
                .version("${org.apache.derby.version")
                .type(DependencyType.DEPENDENCY)
                .inputLocation(new InputLocation(21, 22, is))
                .build());
    }

    @Test
    void TestFileNotFound_ThrowsNoDependencyFileFoundException() {
        assertThatExceptionOfType(NoDependencyFileFoundException.class)
                .isThrownBy(() -> resolver
                        .resolve(new Workspace("src/test/resources/project_root/testDir")));
    }

    @Test
    void TestEmptyDependencies() {
        Workspace ws = new Workspace("src/test/resources/project_root/testDir/empty");
        assertThat(resolver.resolve(ws)).isEmpty();
    }

    @Test
    void TestUnparseableDependencies() {
        assertThatExceptionOfType(DependencyParserException.class).isThrownBy(() ->
                resolver.resolve(new Workspace("src/test/resources/project_root/testDir/parserror")));
    }

    @Test
    void resolve() {
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertThat(plugins).contains(MavenDependency.builder()
                .group(TEST_PLUGIN_GROUP)
                .name(TEST_PLUGIN_NAME)
                .version(TEST_PLUGIN_VERSION)
                .type(DependencyType.PLUGIN)
                .build());
    }

    @Test
    void testSuccessfullyResolvePluginsInPluginManagementSection() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/pluginmanagement");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertThat(plugins).contains(
                MavenDependency.builder()
                        .group(TEST_PLUGIN_GROUP)
                        .name(TEST_PLUGIN_NAME)
                        .version(TEST_PLUGIN_VERSION)
                        .type(DependencyType.PLUGIN)
                        .build());
    }

    @Test
    void testNoPluginsFound_ShouldReturnEmptySet() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/empty");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertThat(plugins).isEmpty();
    }

    @Test
    void testPluginEmptyVersion_ShouldReturnEmptySet() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/noVersion");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertThat(plugins).isEmpty();
    }

    @Test
    void getPluginsWithDefaultGroupId_ShouldSuccessfullyResolve(){
        Set<Dependency> plugins = resolver.resolve(pluginDefaultGroupIdWorkspace);

        assertThat(plugins).contains(MavenDependency.builder()
                .group("org.apache.maven.plugins")
                .name("maven-compiler-plugin")
                .version("3.8.1")
                .type(DependencyType.PLUGIN)
                .build());
    }

    @Test
    void testEmptyBuild_ShouldReturnEmptySet() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/emptyBuild");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertThat(plugins).isEmpty();
    }

    @Test
    void testEmptyPlugins_ShouldReturnEmptySet() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/emptyPlugins");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertThat(plugins).isEmpty();
    }

    @Test
    void testEmptyPluginManagement_ShouldReturnEmptySet() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/emptyPluginManagement");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertThat(plugins).isEmpty();
    }

    @Test
    void testSuccessfullyResolvePluginwithVersionInProperties() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/pluginsWithProperties");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertThat(plugins).contains(MavenDependency.builder()
                .group(TEST_PLUGIN_GROUP)
                .name(TEST_PLUGIN_NAME)
                .version(TEST_PLUGIN_VERSION)
                .type(DependencyType.PLUGIN)
                .build());
    }

    @Test
    void pluginWithNonExistentProperties() {
        pluginWorkspace = new Workspace(pluginWorkspace.getProjectRoot() + "/nonExistentproperties");
        Set<Dependency> plugins = resolver.resolve(pluginWorkspace);
        assertThat(plugins).isEmpty();
    }

    @Test
    void testSuccessfullyResolveParentDependency(){
        assertThat(resolver.resolve(parentDependencyWorkspace))
                .contains(
                        MavenDependency
                                .builder()
                                .name("spring-boot-starter-parent")
                                .group("org.springframework.boot")
                                .version("2.2.5.RELEASE")
                                .type(DependencyType.PARENT_DEPENDENCY)
                                .build());
    }

    @Test
    void testIgnoreInternalDependencies() {
        Workspace workspace = new Workspace("src/test/resources/projectDep");
        assertThat(resolver.resolve(workspace)).isEmpty();
    }
}
