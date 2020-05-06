package com.github.autobump.maven.model;

import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.autobump.maven.model.MavenDependencyResolverTest.TEST_DEPENDENCY_GROUP;
import static com.github.autobump.maven.model.MavenDependencyResolverTest.TEST_DEPENDENCY_NAME;
import static com.github.autobump.maven.model.MavenDependencyResolverTest.TEST_DEPENDENCY_VERSION;
import static com.github.autobump.maven.model.MavenDependencyResolverTest.TEST_PLUGIN_GROUP;
import static com.github.autobump.maven.model.MavenDependencyResolverTest.TEST_PLUGIN_NAME;
import static com.github.autobump.maven.model.MavenDependencyResolverTest.TEST_PLUGIN_VERSION;
import static junit.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MavenDependencyResolverProfileTest {
    private Workspace profilesWorkspace;
    private DependencyResolver resolver;

    @BeforeEach
    void setUp() {
        profilesWorkspace = new Workspace("src/test/resources/profiles_root");
        resolver = new MavenDependencyResolver();
    }

    @Test
    void testProfiles() {

        assertTrue(resolver.resolve(profilesWorkspace).contains(
                MavenDependency.builder()
                        .type(DependencyType.PROFILE_DEPENDENCY)
                        .version(TEST_DEPENDENCY_VERSION)
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .build()
        ));

    }

    @Test
    void testPluginProfiles() {

        assertTrue(resolver.resolve(profilesWorkspace).contains(
                MavenDependency.builder()
                        .type(DependencyType.PROFILE_PLUGIN)
                        .version(TEST_PLUGIN_VERSION)
                        .group(TEST_PLUGIN_GROUP)
                        .name(TEST_PLUGIN_NAME)
                        .build()
        ));

    }

    @Test
    void testDependencyManagementProfiles() {

        assertTrue(resolver.resolve(profilesWorkspace).contains(
                MavenDependency.builder()
                        .type(DependencyType.PROFILE_DEPENDENCY)
                        .version("10.14.2.0")
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .build()
        ));

    }

    @Test
    void testPropertiesInProfiles() {

        assertTrue(resolver.resolve(profilesWorkspace).contains(
                MavenDependency.builder()
                        .type(DependencyType.PROFILE_DEPENDENCY)
                        .version("10.16.2.0")
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .build()
        ));

    }

    @Test
    void testMainPropertiesInProfiles() {

        assertTrue(resolver.resolve(profilesWorkspace).contains(
                MavenDependency.builder()
                        .type(DependencyType.PROFILE_DEPENDENCY)
                        .version("10.17.2.0")
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .build()
        ));

    }

    @Test
    void testEmptyProfile_ShouldRetunNoDependencies() {

        profilesWorkspace = new Workspace(profilesWorkspace.getProjectRoot() + "/empty");
        assertEquals(0, resolver.resolve(profilesWorkspace).size());

    }
}
