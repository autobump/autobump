package model.maven;

import model.Workspace;
import exceptions.NoDependencyFileFoundException;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertNotNull;

public class MavenWorkspaceTest {
    private MavenWorkspace workspace;

    @Before
    public void setUp() throws Exception {
        workspace = new MavenWorkspace("src/test/resources/project_root");
    }

    @Test
    public void getDependencydocument() throws FileNotFoundException {
        assertNotNull(workspace.getDependencyDocument());
    }

    @Test(expected = NoDependencyFileFoundException.class)
    public void getDependencyDocumentFromWrongPath() throws FileNotFoundException {
        Workspace ws = new MavenWorkspace("src/test/resources/project_root/testDir");
        ws.getDependencyDocument();
    }
}