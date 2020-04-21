package model.maven;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import model.exceptions.NoDependencyFileFoundException;
import model.Workspace;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

public class MavenWorkspace implements Workspace {
    private String projectRoot;
    private final String dependencyFileName = "pom.xml";

    public MavenWorkspace(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    @Override
    public Reader getDependencydocument() throws FileNotFoundException {
        return new FileReader(findfile(new File(projectRoot)));
    }

    private File findfile(File file) throws NoDependencyFileFoundException {
        File[] files = file.listFiles();
        if (files != null){
            for (File f : files){
                if (!f.isDirectory() && dependencyFileName.equalsIgnoreCase(f.getName())){
                    return f;
                }
            }
        }
        throw new NoDependencyFileFoundException("Could not find pom.xml in project root");
    }
}
