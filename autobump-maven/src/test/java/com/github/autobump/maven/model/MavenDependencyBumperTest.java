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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                .version(DERBY_VERSION)
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
    void testBump() {
        var dependencies = resolver.resolve(workspace);
        assertTrue(dependencies.contains(MavenDependency.builder()
                .group(DERBY_GROUP)
                .name(DERBY_NAME)
                .version(DERBY_VERSION)
                .type(DependencyType.DEPENDENCY)
                .build()));
        bumpDependency(dependency);
        Dependency updatedDep = MavenDependency.builder()
                .group(DERBY_GROUP)
                .name(DERBY_NAME)
                .version(UPDATED_VERSION)
                .type(DependencyType.DEPENDENCY)
                .build();
        dependencies = resolver.resolve(workspace);
        assertTrue(dependencies.contains(updatedDep));
    }



    @Test
    void testBumpProperty() {
        Dependency dependency = MavenDependency.builder()
                .name("derbys")
                .group(DERBY_GROUP)
                .version(DERBY_VERSION)
                .type(DependencyType.DEPENDENCY)
                .build();
        var dependencies = resolver.resolve(workspace);
        assertTrue(dependencies.contains(dependency));
        bumpDependency(dependency);
        Dependency updatedDep = MavenDependency.builder()
                .group(DERBY_GROUP)
                .name("derbys")
                .version(UPDATED_VERSION)
                .type(DependencyType.DEPENDENCY)
                .build();
        dependencies = resolver.resolve(workspace);
        assertTrue(dependencies.contains(updatedDep));
    }

    @Test
    void testThrowsIO() {
        mavenDependencyBumper = new MavenDependencyBumperTester();
        assertThrows(UncheckedIOException.class, () ->
                mavenDependencyBumper.bump(new Workspace("src/test/resources/project_root"),
                        new Bump(dependency, version)));
    }

    @Test
    void testThrowParser(){
        mavenDependencyBumper = new MavenDependencyBumper();
        mavenDependencyBumper.mavenXpp3ReaderEx = new MavenXpp3ReaderExTester();
        assertThrows(DependencyParserException.class, () ->
                mavenDependencyBumper.bump(new Workspace("src/test/resources/project_root"),
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
                .version(DERBY_VERSION)
                .build();
        assertTrue(dependencies.contains(dependency));
        bumpDependency(dependency);
        Dependency updatedDep = MavenDependency.builder()
                .group(DERBY_GROUP)
                .name(DERBY_NAME)
                .version(UPDATED_VERSION)
                .type(DependencyType.DEPENDENCY)
                .build();
        dependencies = resolver.resolve(workspace);
        assertTrue(dependencies.contains(updatedDep));
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

    @Test
    void testBumpNormalDependency() {
        var dependencies = resolver.resolve(workspace);
        MavenDependency dependency = MavenDependency.builder()
                .group(DERBY_GROUP)
                .type(DependencyType.DEPENDENCY)
                .name(DERBY_NAME)
                .version(DERBY_VERSION)
                .build();
        assertTrue(dependencies.contains(dependency));
        bumpDependency(dependency.getAsDependency());
        Dependency updatedDep = MavenDependency.builder()
                .group(DERBY_GROUP)
                .name(DERBY_NAME)
                .version(UPDATED_VERSION)
                .type(DependencyType.DEPENDENCY)
                .build();
        dependencies = resolver.resolve(workspace);
        assertTrue(dependencies.contains(updatedDep));
    }
}
