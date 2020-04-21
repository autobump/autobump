package model.maven;

import lombok.extern.log4j.Log4j2;
import model.Dependency;
import model.DependencyResolver;
import model.Workspace;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
public class MavenDependencyResolver implements DependencyResolver {


    @Override
    public Set<Dependency> resolve(Workspace workspace) throws IOException, XmlPullParserException {

        Reader dependencyDocument = workspace.getDependencydocument();
        return new MavenXpp3Reader()
                .read(dependencyDocument)
                .getDependencies()
                .stream()
                .filter(dependency -> dependency.getVersion() != null)
                .map(dependency -> new Dependency(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion()))
                .collect(Collectors.toSet());
    }
}
