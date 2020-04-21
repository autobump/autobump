package model.maven;

import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import model.Dependency;
import model.DependencyResolver;
import model.Workspace;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3ReaderEx;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class MavenDependencyResolver implements DependencyResolver {


    @Override
    public Set<Dependency> resolve(Workspace workspace) {
        try {
            Reader dependencyDocument = workspace.getDependencydocument();
            return new MavenXpp3Reader()
                    .read(dependencyDocument)
                    .getDependencies()
                    .stream()
                    .map(dependency -> new Dependency(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion()))
                    .collect(Collectors.toSet());
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            
            // TODO GS: 21/04/2020 log error
        }
        return new HashSet<>();
    }
}
