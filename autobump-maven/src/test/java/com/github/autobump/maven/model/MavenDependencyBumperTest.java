package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.DependencyParserException;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.maven.model.testclasses.MavenDependencyBumperTester;
import com.github.autobump.maven.model.testclasses.MavenXpp3ReaderExTester;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.InputSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class MavenDependencyBumperTest {
    private static final String DERBY_GROUP = "org.apache.derby";
    private static final String DERBY_NAME = "derby";
    private static final String DERBY_VERSION = "10.15.2.0";
    private static final String UPDATED_VERSION = "bumpTest";
    private MavenDependencyBumper mavenDependencyBumper;
    private Workspace workspace;
    private MavenDependencyResolver resolver;
    private Dependency dependency;
    private Version version;

    @BeforeEach
    void setUp() throws IOException {
        version = new MavenVersion(UPDATED_VERSION);
        dependency = MavenDependency.builder()
                .group(DERBY_GROUP)
                .name(DERBY_NAME)
                .version(new MavenVersion(DERBY_VERSION))
                .type(DependencyType.DEPENDENCY)
                .build();
        mavenDependencyBumper = new MavenDependencyBumper();
        Path tempDirPath = Files.createTempDirectory(null);
        Files.copy(Path.of("src/test/resources/project_root/testBump/pom.xml"),
                Path.of(tempDirPath.toString() + File.separator + "pom.xml"));
        workspace = new Workspace(tempDirPath.toString());
        resolver = new MavenDependencyResolver();
    }

    @Test
    void testBump_OneDependencyShouldBeUpdated() {
        var dependencies = resolver.resolve(workspace);
        assertThat(dependencies).contains(
                MavenDependency.builder()
                .group(DERBY_GROUP)
                .name(DERBY_NAME)
                .version(new MavenVersion(DERBY_VERSION))
                .type(DependencyType.DEPENDENCY)
                .build());
        bumpDependency(dependency);
        dependencies = resolver.resolve(workspace);
        assertThat(dependencies).contains(MavenDependency.builder()
                .group(DERBY_GROUP)
                .name(DERBY_NAME)
                .version(new MavenVersion(UPDATED_VERSION))
                .type(DependencyType.DEPENDENCY)
                .build());
    }

    @Test
    void testBumpWithDependencyThatHasVersionNumberInProperty_AssertThatVersionUpdated() {
        var dependencies = resolver.resolve(workspace);
        var dep = MavenDependency.builder()
                .name("derbys")
                .group(DERBY_GROUP)
                .version(new MavenVersion(DERBY_VERSION))
                .type(DependencyType.DEPENDENCY)
                .build();
        assertThat(dependencies).contains(dep);

        bumpDependency(dep);

        dependencies = resolver.resolve(workspace);
        assertThat(dependencies).contains(
                MavenDependency.builder()
                .group(DERBY_GROUP)
                .name("derbys")
                .version(new MavenVersion(UPDATED_VERSION))
                .type(DependencyType.DEPENDENCY)
                .build());
    }

    @Test
    void testWhenIOException_NewUncheckedIOExceptionIsThrown() {
        mavenDependencyBumper = new MavenDependencyBumperTester();
        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy( () -> mavenDependencyBumper
                        .bump(new Workspace("src/test/resources/project_root"),
                                new Bump(dependency, version)));
    }

    @Test
    void testThrowParser(){
        mavenDependencyBumper = new MavenDependencyBumper();
        mavenDependencyBumper.mavenXpp3ReaderEx = new MavenXpp3ReaderExTester();
        assertThatExceptionOfType(DependencyParserException.class)
                .isThrownBy(() -> mavenDependencyBumper
                        .bump(new Workspace("src/test/resources/project_root"),
                                new Bump(dependency, version)));
    }

    @Test
    void testBumpMavenDependency() {
        var dependencies = resolver.resolve(workspace);
        InputLocation inputLocation = getInputLocation(43, 22);
        Dependency dependency = MavenDependency.builder()
                .inputLocation(inputLocation)
                .group(DERBY_GROUP)
                .type(DependencyType.DEPENDENCY)
                .name(DERBY_NAME)
                .version(new MavenVersion(DERBY_VERSION))
                .build();
        assertThat(dependencies).contains(dependency);
        bumpDependency(dependency);
        dependencies = resolver.resolve(workspace);
        assertThat(dependencies).contains(MavenDependency.builder()
                .group(DERBY_GROUP)
                .name(DERBY_NAME)
                .version(new MavenVersion(UPDATED_VERSION))
                .type(DependencyType.DEPENDENCY)
                .build());
    }

    @Test
    void testBumpNormalDependency() {
        var dependencies = resolver.resolve(workspace);
        MavenDependency dependency = MavenDependency.builder()
                .group(DERBY_GROUP)
                .type(DependencyType.DEPENDENCY)
                .name(DERBY_NAME)
                .version(new MavenVersion(DERBY_VERSION))
                .build();
        assertThat(dependencies).contains(dependency);
        bumpDependency(dependency.getAsDependency());
        dependencies = resolver.resolve(workspace);
        assertThat(dependencies).contains(MavenDependency.builder()
                .group(DERBY_GROUP)
                .name(DERBY_NAME)
                .version(new MavenVersion(UPDATED_VERSION))
                .type(DependencyType.DEPENDENCY)
                .build());
    }

    private void bumpDependency(Dependency dependency) {
        Bump bump = new Bump(dependency, version);
        mavenDependencyBumper.bump(workspace, bump);
    }

    private InputLocation getInputLocation(int lineNumber, int comumnNumber) {
        InputSource inputSource = new InputSource();
        inputSource.setLocation(workspace.getProjectRoot() + File.separator + "pom.xml");
        return new InputLocation(lineNumber, comumnNumber, inputSource);
    }

}
