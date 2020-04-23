package model.maven;

import exceptions.NoDependencyFileFoundException;
import model.Workspace;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Paths;

public class MavenWorkspace implements Workspace {
    private static final String DEPENDENCY_FILE_NAME = "pom.xml";
    private final String projectRoot;

    public MavenWorkspace(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    @Override
    public Reader getDependencyDocument() {
        try {
            return new FileReader(Paths.get(projectRoot, MavenWorkspace.DEPENDENCY_FILE_NAME).toFile());
        } catch (FileNotFoundException e) {
            throw new NoDependencyFileFoundException("Reader could not load pom file", e);
        }
    }
}
