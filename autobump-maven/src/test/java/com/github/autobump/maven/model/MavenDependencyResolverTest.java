package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.DependencyParserException;
import com.github.autobump.core.exceptions.NoDependencyFileFoundException;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MavenDependencyResolverTest {

    private static final transient String TEST_DEPENDENCY_GROUP = "org.apache.derby";
    private static final transient String TEST_DEPENDENCY_NAME = "derby";
    private transient Workspace workspace;
    private transient DependencyResolver dependencyResolver;

    @BeforeEach
    public void setUp() {
        workspace = new Workspace("src/test/resources/project_root");
        dependencyResolver = new MavenDependencyResolver();
    }

    @Test
    public void TestSuccesresolve() {
        Set<Dependency> deps = dependencyResolver.resolve(workspace);
        assertEquals(
                Set.of(Dependency.builder()
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .version("10.15.2.0")
                        .build()),
                deps);
    }

    @Test
    public void TestSuccesresolveProperties() {
        Workspace ws = new Workspace("src/test/resources/project_root_support_properties");
        Set<Dependency> deps = dependencyResolver.resolve(ws);
        assertEquals(
                Set.of(Dependency.builder()
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .version("10.15.2.0")
                        .build()),
                deps);
    }

    @Test
    public void TestresolveUndevinedProperty() {
        Workspace ws = new Workspace("src/test/resources/project_root_support_properties_undefinedproperty");
        Set<Dependency> deps = dependencyResolver.resolve(ws);
        assertEquals(
                Set.of(Dependency.builder()
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .version("${org.apache.derby.version}")
                        .build()),
                deps);
    }

    @Test
    public void TestresolveMalformedVarProperty() {
        Workspace ws = new Workspace("src/test/resources/project_root_support_properties_badproperty");
        Set<Dependency> deps = dependencyResolver.resolve(ws);
        assertEquals(
                Set.of(Dependency.builder()
                        .group(TEST_DEPENDENCY_GROUP)
                        .name(TEST_DEPENDENCY_NAME)
                        .version("${org.apache.derby.version")
                        .build()),
                deps);
    }

    @Test
    public void TestFileNotFound() {
        assertThrows(NoDependencyFileFoundException.class, () ->
                dependencyResolver.resolve(new Workspace("src/test/resources/project_root/testDir")));
    }

    @Test
    public void TestEmpyDependencies() {
        Workspace ws = new Workspace("src/test/resources/project_root/testDir/empty");
        assertEquals(Set.of(), dependencyResolver.resolve(ws));
    }

    @Test
    public void TestUnparseableDependencies() {
        assertThrows(DependencyParserException.class, () ->
                dependencyResolver.resolve(new Workspace("src/test/resources/project_root/testDir/parserror")));
    }
}
