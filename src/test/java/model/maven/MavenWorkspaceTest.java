package model.maven;

import model.Workspace;
import model.exceptions.NoDependencyFileFoundException;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class MavenWorkspaceTest {
    private MavenWorkspace workspace;

    @Before
    public void setUp() throws Exception {
        workspace = new MavenWorkspace("src/test/resources/project_root");
    }

    @Test
    public void getDependencydocument() throws FileNotFoundException {
        assertNotNull(workspace.getDependencydocument());
    }

    @Test(expected = NoDependencyFileFoundException.class)
    public void getDependencyDocumentFromWrongPath() throws FileNotFoundException {
        Workspace ws = new MavenWorkspace("src/test/resources/project_root/testDir");
        ws.getDependencydocument();
    }
}