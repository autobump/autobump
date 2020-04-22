package model.maven;

import exceptions.NoDependencyFileFoundException;
import model.Workspace;

import java.io.File;
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
            return new FileReader(findfile(Paths.get(projectRoot).toFile()));
        } catch (FileNotFoundException e) {
            throw new NoDependencyFileFoundException("Reader could not load pom file", e);
        }
    }

    private File findfile(File file) {
        File[] files = file.listFiles();
        if (files != null){
            for (File f : files){
                if (!f.isDirectory() && MavenWorkspace.DEPENDENCY_FILE_NAME.equalsIgnoreCase(f.getName())){
                    return f;
                }
            }
        }
        throw new NoDependencyFileFoundException("Could not find pom.xml in project root", new FileNotFoundException());
    }
}
