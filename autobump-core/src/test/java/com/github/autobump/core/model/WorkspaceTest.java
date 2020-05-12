package com.github.autobump.core.model;

import com.github.autobump.core.exceptions.NoDependencyFileFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class WorkspaceTest {
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        workspace = new Workspace("src/test/resources/project_root");
    }

    @Test
    void getDependencyDocument() {
        assertThat(workspace.getDependencyDocument("pom.xml")).isNotNull();
    }

    @Test
    void getDependencyNonexistentDocument_ShouldThrowException() {
        assertThatExceptionOfType(NoDependencyFileFoundException.class)
                .isThrownBy(() -> workspace.getDependencyDocument("build.gradle"));
    }

    @Test
    void walkFilesTest(){
        DependencyResolver dependencyResolver = Mockito.mock(DependencyResolver.class);
        Mockito.when(dependencyResolver.getBuildFileName()).thenReturn("pom.xml");

        Set<Dependency> dependencies = new HashSet<>();
        Set<Dependency> ignored = new HashSet<>();

        workspace.walkFiles(dependencies, ignored, dependencyResolver);

        assertThat(dependencies).hasSize(0);
        assertThat(ignored).hasSize(0);
    }

}
