package com.github.autobump.maven.model;

import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.Workspace;
import org.apache.maven.model.Model;

import java.util.Set;
import java.util.stream.Collectors;

public class MavenDependencyResolver implements DependencyResolver {
    private final transient MavenModelAnalyser mavenModelAnalyser;

    public MavenDependencyResolver() {
        this.mavenModelAnalyser = new MavenModelAnalyser();
    }

    @Override
    public Set<Dependency> resolve(Workspace workspace) {
        Model model = mavenModelAnalyser.getModel(workspace);
        return model
                .getDependencies()
                .stream()
                .filter(dependency -> dependency.getVersion() != null)
                .map(dependency -> Dependency.builder()
                        .group(dependency.getGroupId())
                        .name(dependency.getArtifactId())
                        .version(mavenModelAnalyser.getVersionFromProperties(model, dependency.getVersion()))
                        .build())
                .filter(dependency -> dependency.getVersion() != null)
                .collect(Collectors.toUnmodifiableSet());
    }
}
