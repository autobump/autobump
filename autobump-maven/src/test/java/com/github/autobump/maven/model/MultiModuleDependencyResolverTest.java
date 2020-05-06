package com.github.autobump.maven.model;

import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class MultiModuleDependencyResolverTest {

    private Workspace multiModuleWorkspace;
    private MavenDependencyResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new MavenDependencyResolver();
        multiModuleWorkspace = new Workspace("src/test/resources/multi_module_root");
    }

    @Test
    void testResolveMultiModuleProject() {
        Set<Dependency> dependencies = resolver.resolve(multiModuleWorkspace);
        assertThat(dependencies).hasSize(3);
    }

    @Test
    void testResolveMultiModuleProject_withDependencyManagementSection() {
        Workspace ws = new Workspace("src/test/resources/multi_module_root_depmngt");
        Set<Dependency> dependencies = resolver.resolve(ws);
        assertThat(dependencies).hasSize(4);
    }

    @Test
    void testThrowsIO() {

        class MavenDependencyResolverTester extends MavenDependencyResolver {
            @Override
            public void walkFiles(Workspace workspace, Set<Dependency> dependencies, Set<Dependency> toBeIgnored)
                    throws IOException {
                throw new IOException();
            }
        }

        MavenDependencyResolverTester tester = new MavenDependencyResolverTester();

        assertThatExceptionOfType(UncheckedIOException.class).isThrownBy(() ->
                tester.resolve(multiModuleWorkspace));
    }
}
