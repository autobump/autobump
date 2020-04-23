package model;

import exceptions.NoDependencyFileFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class MavenWorkspaceTest {
    private transient MavenWorkspace workspace;

    @BeforeEach
    void setUp() throws Exception {
        workspace = new MavenWorkspace("src/test/resources/project_root");
    }

    @Test
    void getDependencydocument() {
        assertNotNull(workspace.getDependencyDocument());
    }

    @Test
    void getDependencyDocumentFromWrongPath() {
        Workspace ws = new MavenWorkspace("src/test/resources/project_root/testDir");
        assertThrows(NoDependencyFileFoundException.class, ws::getDependencyDocument);
    }
}
