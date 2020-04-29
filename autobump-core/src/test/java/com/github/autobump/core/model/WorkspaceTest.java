package com.github.autobump.core.model;

import com.github.autobump.core.exceptions.NoDependencyFileFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkspaceTest {
    private  Workspace workspace;

    @BeforeEach
    void setUp() {
        workspace = new Workspace("src/test/resources/project_root");
    }

    @Test
    void getDependencyDocument() {
        assertNotNull(workspace.getDependencyDocument("pom.xml"));
    }

    @Test
    void wrongDependencytest() {
        assertThrows(NoDependencyFileFoundException.class, () -> workspace.getDependencyDocument("build.gradle"));
    }
}
