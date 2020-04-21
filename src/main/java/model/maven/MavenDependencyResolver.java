package model.maven;

import model.Dependency;
import model.DependencyResolver;
import model.Workspace;

import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;


public class MavenDependencyResolver implements DependencyResolver {
    @Override
    public Set<Dependency> resolve(Workspace workspace) {
        try {
            Reader dependencyDocument = workspace.getDependencydocument();

        } catch (FileNotFoundException e) {
            return new HashSet<>();
        }
    }
}
