package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.NoDependencyFileFoundException;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class MavenWorkspaceTest {
    private transient Workspace workspace;

    @BeforeEach
    void setUp() throws Exception {
        workspace = new Workspace("src/test/resources/project_root");
    }

    @Test
    void getDependencydocument() {
        assertNotNull(workspace.getDependencyDocument("pom.xml"));
    }

    @Test
    void getDependencyDocumentFromWrongPath() {
        assertThrows(NoDependencyFileFoundException.class,
                () ->
                    new Workspace("src/test/resources/project_root/testDir").getDependencyDocument("pom.xml"));
    }
}
