package model.maven;

import exceptions.DependencyParserException;
import exceptions.NoDependencyFileFoundException;
import model.Dependency;
import model.DependencyResolver;
import model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MavenDependencyResolverTest {

    private transient Workspace workspace;
    private transient DependencyResolver dependencyResolver;

    @BeforeEach
    public void setUp() {
        workspace = new MavenWorkspace("src/test/resources/project_root");
        dependencyResolver = new MavenDependencyResolver();
    }

    @Test
    public void TestSuccesresolve() {
        Set<Dependency> deps = dependencyResolver.resolve(workspace);
        assertEquals(
                Set.of(Dependency.builder()
                .group("org.apache.derby")
                .name("derby")
                .version("10.15.2.0")
                .build()),
                deps);
    }

    @Test
    public void TestFileNotFound() {
        Workspace ws = new MavenWorkspace("src/test/resources/project_root/testDir");
        assertThrows(NoDependencyFileFoundException.class, () -> dependencyResolver.resolve(ws));

    }

    @Test
    public void TestEmpyDependencies() {
        Workspace ws = new MavenWorkspace("src/test/resources/project_root/testDir/empty");
        assertEquals(Set.of(), dependencyResolver.resolve(ws));
    }

    @Test
    public void TestUnparseableDependencies(){
        Workspace ws = new MavenWorkspace("src/test/resources/project_root/testDir/parserror");
        assertThrows(DependencyParserException.class, () -> dependencyResolver.resolve(ws));
    }
}