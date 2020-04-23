package model;

import exceptions.NoDependencyFileFoundException;
import lombok.Getter;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

@Getter
public class MavenWorkspace implements Workspace {
    private static final String DEPENDENCY_FILE_NAME = "pom.xml";
    private final String projectRoot;

    public MavenWorkspace(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    @Override
    public Reader getDependencyDocument() {
        try {
            return Files.newBufferedReader(Paths.get(projectRoot, MavenWorkspace.DEPENDENCY_FILE_NAME));
        } catch (IOException e) {
            throw new NoDependencyFileFoundException("Reader could not load pom file", e);
        }
    }
}
