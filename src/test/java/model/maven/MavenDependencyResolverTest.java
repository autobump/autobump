package model.maven;

import model.Dependency;
import model.DependencyResolver;
import model.Workspace;
import model.exceptions.NoDependencyFileFoundException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Test;


import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class MavenDependencyResolverTest {

    private Workspace workspace;
    private DependencyResolver dependencyResolver;

    @Before
    public void setUp() {
        workspace = new MavenWorkspace("src/test/resources/project_root");
        dependencyResolver = new MavenDependencyResolver();
    }

    @Test
    public void TestSuccesresolve() {
        Set<Dependency> deps = null;
        try {
            deps = dependencyResolver.resolve(workspace);
        } catch (IOException | XmlPullParserException e) {
            fail();
        }
        assertEquals(Set.of(new Dependency("org.apache.derby", "derby", "10.15.2.0")), deps);
    }

    @Test(expected = NoDependencyFileFoundException.class)
    public void TestFileNotFound() throws IOException, XmlPullParserException {
        Workspace ws = new MavenWorkspace("src/test/resources/project_root/testDir");
        dependencyResolver.resolve(ws);
    }

    @Test
    public void TestEmpyDependencies() throws IOException, XmlPullParserException {
        Workspace ws = new MavenWorkspace("src/test/resources/project_root/testDir/empty");
        assertEquals(Set.of(), dependencyResolver.resolve(ws));
    }

}