package model.maven;

import model.Workspace;
import model.exceptions.NoDependencyFileFoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Paths;

public class MavenWorkspace implements Workspace {
    private String projectRoot;
    private final static String dependencyFileName = "pom.xml";

    public MavenWorkspace(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    @Override
    public Reader getDependencydocument() throws FileNotFoundException {
        return new FileReader(findfile(Paths.get(projectRoot).toFile()));
    }

    private File findfile(File file) throws NoDependencyFileFoundException {
        File[] files = file.listFiles();
        if (files != null){
            for (File f : files){
                if (!f.isDirectory() && MavenWorkspace.dependencyFileName.equalsIgnoreCase(f.getName())){
                    return f;
                }
            }
        }
        throw new NoDependencyFileFoundException("Could not find pom.xml in project root");
    }
}
