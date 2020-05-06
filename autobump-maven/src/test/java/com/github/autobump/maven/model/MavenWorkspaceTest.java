package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.NoDependencyFileFoundException;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


public class MavenWorkspaceTest {
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
    void getDependencyDocumentFromWrongPath() {
        assertThatExceptionOfType(NoDependencyFileFoundException.class).isThrownBy(() ->
                    new Workspace("src/test/resources/project_root/testDir")
                            .getDependencyDocument("pom.xml"));
    }
}
