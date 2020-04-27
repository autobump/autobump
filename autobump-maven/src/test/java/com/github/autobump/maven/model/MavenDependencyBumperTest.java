package com.github.autobump.maven.model;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MavenDependencyBumperTest {
    private transient MavenDependencyBumper mavenDependencyBumper;
    private transient Workspace workspace;
    private transient MavenDependencyResolver resolver;

    @BeforeEach
    void setUp() throws IOException {
        mavenDependencyBumper = new MavenDependencyBumper();
        Files.copy(Path.of("src/test/resources/project_root/testBump/pom.xml"), Path.of("src/test/resources/pom.xml"));
        workspace = new Workspace("src/test/resources");
        resolver = new MavenDependencyResolver();
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.delete(Path.of("src/test/resources/pom.xml"));
    }

    @Test
    void testBump() {
        var dependencies = resolver.resolve(workspace);
        Dependency dep = Dependency.builder().group("org.apache.derby").name("derby").version("10.15.2.0").build();
        assertTrue(dependencies.contains(dep));
        Bump bump = new Bump(dep, new Version("bumpTest"));
        mavenDependencyBumper.bump(workspace, bump);
        Dependency updatedDep = Dependency.builder()
                .group("org.apache.derby")
                .name("derby")
                .version("bumpTest")
                .build();
        dependencies = resolver.resolve(workspace);
        assertTrue(dependencies.contains(updatedDep));
    }
}
