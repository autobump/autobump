package model;

import exceptions.DependencyParserException;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;
import java.util.stream.Collectors;

public class MavenDependencyResolver implements DependencyResolver {


    @Override
    public Set<Dependency> resolve(Workspace workspace) {

        try(Reader dependencyDocument = workspace.getDependencyDocument()){
            return new MavenXpp3Reader()
                    .read(dependencyDocument)
                    .getDependencies()
                    .stream()
                    .filter(dependency -> dependency.getVersion() != null)
                    .map(dependency -> Dependency.builder()
                            .group(dependency.getGroupId())
                            .name(dependency.getArtifactId())
                            .version(dependency.getVersion())
                            .build())
                    .collect(Collectors.toSet());
        } catch (XmlPullParserException | IOException e) {
            throw new DependencyParserException("Parser threw an error.", e);
        }
    }


}
