package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.DependencyParserException;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.maven.model.testclasses.MavenDependencyBumperTester;
import com.github.autobump.maven.model.testclasses.MavenXpp3ReaderExTester;
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
    private transient MavenDependencyBumper mavenDependencyBumper;
    private transient Workspace workspace;
    private transient MavenDependencyResolver resolver;
    private transient Dependency dependency;
    private transient Version version;
    private static final transient String DERBY_GROUP = "org.apache.derby";

    @BeforeEach
    void setUp() throws IOException {
        version = new Version("bumpTest");
        dependency = Dependency.builder().group(DERBY_GROUP).name("derby").version("10.15.2.0").build();
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
        //assertTrue(dependencies.contains(dependency));
        Bump bump = new Bump(dependency, version);
        mavenDependencyBumper.bump(workspace, bump);
        Dependency updatedDep = Dependency.builder()
                .group(DERBY_GROUP)
                .name("derby")
                .version("bumpTest")
                .build();
        dependencies = resolver.resolve(workspace);
        assertTrue(dependencies.contains(updatedDep));
    }

    @Test
    void testBumpProperty() {
        Dependency dependency = Dependency.builder()
                .name("derbys")
                .group(DERBY_GROUP)
                .version("10.15.2.0")
                .build();
        var dependencies = resolver.resolve(workspace);
        //assertTrue(dependencies.contains(dependency));
        Bump bump = new Bump(dependency, version);
        mavenDependencyBumper.bump(workspace, bump);
        Dependency updatedDep = Dependency.builder()
                .group(DERBY_GROUP)
                .name("derby")
                .version("bumpTest")
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
}
