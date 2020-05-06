package com.github.autobump.core.model;

import com.github.autobump.core.exceptions.NoDependencyFileFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class WorkspaceTest {
    private  Workspace workspace;

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
}
