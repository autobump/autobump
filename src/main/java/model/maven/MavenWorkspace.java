package model.maven;

import model.Workspace;
import model.exceptions.NoDependencyFileFoundException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.nio.file.Paths;

public class MavenWorkspace implements Workspace {
    private final static String DEPENDENCY_FILE_NAME = "pom.xml";
    private final String projectRoot;

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
                if (!f.isDirectory() && MavenWorkspace.DEPENDENCY_FILE_NAME.equalsIgnoreCase(f.getName())){
                    return f;
                }
            }
        }
        throw new NoDependencyFileFoundException("Could not find pom.xml in project root");
    }
}
