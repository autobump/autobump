package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.DependencyParserException;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.Workspace;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;
import java.util.stream.Collectors;

public class MavenDependencyResolver implements DependencyResolver {
    private static final String DEPENDENCY_FILENAME = "pom.xml";

    @Override
    public Set<Dependency> resolve(Workspace workspace) {
        Model model = getModel(workspace);
        return model
                .getDependencies()
                .stream()
                .filter(dependency -> dependency.getVersion() != null)
                .map(dependency -> Dependency.builder()
                        .group(dependency.getGroupId())
                        .name(dependency.getArtifactId())
                        .version(getDependencyVersionFromModel(model, dependency.getVersion()))
                        .build())
                .collect(Collectors.toSet());
    }

    private Model getModel(Workspace workspace) {
        try (Reader dependencyDocument = workspace.getDependencyDocument(MavenDependencyResolver.DEPENDENCY_FILENAME)) {
            return new MavenXpp3Reader()
                    .read(dependencyDocument);
        } catch (XmlPullParserException | IOException e) {
            throw new DependencyParserException("Parser threw an error.", e);
        }
    }

    private String getDependencyVersionFromModel(Model model, String dependencyVersionData) {
        if (!isProperty(dependencyVersionData)) {
            return dependencyVersionData;
        }
        String version = model.getProperties().getProperty(getPropertyName(dependencyVersionData));
        if (version == null) {
            throw new RuntimeException("Error reading version property");
        }
        return version;
    }

    private boolean isProperty(String dependencyVersionData) {
        return dependencyVersionData.startsWith("${") && dependencyVersionData.endsWith("}");
    }

    private String getPropertyName(String dependencyVersionData) {
        return dependencyVersionData.substring(2, dependencyVersionData.length() - 1);
    }
}
