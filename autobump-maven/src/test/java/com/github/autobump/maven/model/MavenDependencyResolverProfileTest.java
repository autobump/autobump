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
import static org.assertj.core.api.Assertions.assertThat;

public class MavenDependencyResolverProfileTest {
    private Workspace profilesWorkspace;
    private DependencyResolver resolver;

    @BeforeEach
    void setUp() {
        profilesWorkspace = new Workspace("src/test/resources/profiles_root");
        resolver = new MavenDependencyResolver(new MavenModelAnalyser());
    }

    @Test
    void testProfiles() {

        assertThat(resolver.resolve(profilesWorkspace)).contains(
                MavenDependency.builder()
                        .type(DependencyType.PROFILE_DEPENDENCY)
                        .version(TEST_DEPENDENCY_VERSION)
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .build()
        );
    }

    @Test
    void testPluginProfiles() {

        assertThat(resolver.resolve(profilesWorkspace)).contains(
                MavenDependency.builder()
                        .type(DependencyType.PROFILE_PLUGIN)
                        .version(TEST_PLUGIN_VERSION)
                        .group(TEST_PLUGIN_GROUP)
                        .name(TEST_PLUGIN_NAME)
                        .build()
        );
    }

    @Test
    void testDependencyManagementProfiles() {

        assertThat(resolver.resolve(profilesWorkspace)).contains(
                MavenDependency.builder()
                        .type(DependencyType.PROFILE_DEPENDENCY)
                        .version(new MavenVersion("10.14.2.0"))
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .build()
        );
    }

    @Test
    void testPropertiesInProfiles() {

        assertThat(resolver.resolve(profilesWorkspace)).contains(
                MavenDependency.builder()
                        .type(DependencyType.PROFILE_DEPENDENCY)
                        .version(new MavenVersion("10.16.2.0"))
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .build()
        );
    }

    @Test
    void testMainPropertiesInProfiles() {

        assertThat(resolver.resolve(profilesWorkspace)).contains(
                MavenDependency.builder()
                        .type(DependencyType.PROFILE_DEPENDENCY)
                        .version(new MavenVersion("10.17.2.0"))
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .build()
        );
    }

    @Test
    void testEmptyProfile_ShouldRetunNoDependencies() {

        profilesWorkspace = new Workspace(profilesWorkspace.getProjectRoot() + "/empty");
        assertThat(resolver.resolve(profilesWorkspace).size()).isEqualTo(0);

    }
}
