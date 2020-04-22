package model.maven;

import exceptions.DependencyParserException;
import exceptions.NoDependencyFileFoundException;
import model.Dependency;
import model.DependencyResolver;
import model.Workspace;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;

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
        deps = dependencyResolver.resolve(workspace);
        assertEquals(
                Set.of(Dependency.builder()
                .group("org.apache.derby")
                .name("derby")
                .version("10.15.2.0")
                .build()),
                deps);
    }

    @Test(expected = NoDependencyFileFoundException.class)
    public void TestFileNotFound() {
        Workspace ws = new MavenWorkspace("src/test/resources/project_root/testDir");
        dependencyResolver.resolve(ws);
    }

    @Test
    public void TestEmpyDependencies() {
        Workspace ws = new MavenWorkspace("src/test/resources/project_root/testDir/empty");
        assertEquals(Set.of(), dependencyResolver.resolve(ws));
    }

    @Test(expected = DependencyParserException.class)
    public void TestUnparseableDependencies(){
        Workspace ws = new MavenWorkspace("src/test/resources/project_root/testDir/parserror");
        dependencyResolver.resolve(ws);
    }
}