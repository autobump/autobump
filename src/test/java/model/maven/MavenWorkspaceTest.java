package model.maven;

import exceptions.NoDependencyFileFoundException;
import model.Workspace;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class MavenWorkspaceTest {
    private MavenWorkspace workspace;

    @Before
    public void setUp() throws Exception {
        workspace = new MavenWorkspace("src/test/resources/project_root");
    }

    @Test
    public void getDependencydocument() {
        assertNotNull(workspace.getDependencyDocument());
    }

    @Test(expected = NoDependencyFileFoundException.class)
    public void getDependencyDocumentFromWrongPath() {
        Workspace ws = new MavenWorkspace("src/test/resources/project_root/testDir");
        ws.getDependencyDocument();
    }
}