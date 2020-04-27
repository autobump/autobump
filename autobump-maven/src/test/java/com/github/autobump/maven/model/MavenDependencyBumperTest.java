package com.github.autobump.maven.model;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class MavenDependencyBumperTest {
    private transient MavenDependencyBumper mavenDependencyBumper;

    @BeforeEach
    void setUp() throws IOException {
        mavenDependencyBumper = new MavenDependencyBumper();
        Files.copy(Path.of("src/test/resources/project_root/pom.xml"), Path.of("src/test/resources/pom.xml"));
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.delete(Path.of("src/test/resources/pom.xml"));
    }

    @Test
    void testBump() {
        Workspace ws = new Workspace("src/test/resources");
        MavenDependencyResolver resolver = new MavenDependencyResolver();
        var dependencies = resolver.resolve(ws);
        Bump bump = new Bump(dependencies.stream().findFirst().orElseThrow(), new Version("bumpTest"));
        mavenDependencyBumper.bump(ws, bump);
    }
}
