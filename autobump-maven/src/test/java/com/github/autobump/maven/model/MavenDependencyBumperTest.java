package com.github.autobump.maven.model;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MavenDependencyBumperTest {
    private transient MavenDependencyBumper mavenDependencyBumper;

    @BeforeEach
    void setUp() {
        mavenDependencyBumper = new MavenDependencyBumper();
    }

    @Test
    void bump() {
        Workspace ws = new Workspace("src/test/resources/project_root");
        MavenDependencyResolver resolver = new MavenDependencyResolver();
        var dependencies = resolver.resolve(ws);
        Bump bump = new Bump(dependencies.stream().findFirst().orElseThrow(), new Version("test"));
        mavenDependencyBumper.bump(ws, bump);
    }
}
