package com.github.autobump.core.model;

import lombok.Value;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Value
public class Bump {
    Set<Dependency> dependencies;
    Version updatedVersion;

    public Bump(Dependency dependency, Version version) {
        this.dependencies = Set.of(dependency);
        this.updatedVersion = version;
    }

    public Bump(Set<Dependency> dependencies, Version version) {
        this.dependencies = dependencies;
        this.updatedVersion = version;
    }

    public String getGroup() {
        return dependencies.stream().findFirst().map(Dependency::getGroup).orElse(null);
    }

    public static Bump combine(Bump bump, Bump anotherBump) {
        return new Bump(Stream.concat(bump.getDependencies().stream(),
                anotherBump.getDependencies().stream())
                .collect(Collectors.toUnmodifiableSet()),
                bump.getUpdatedVersion());
    }

    public String getTitle() {
        StringBuilder titleBuilder = new StringBuilder("Bumped ");
        if (isMultiBump()) {
            titleBuilder.append(addDependenciesToTitle());
        } else {
            for (Dependency dependency : dependencies) {
                titleBuilder.append(String.format("%s:%s", dependency.getGroup(), dependency.getName()));
            }
        }
        titleBuilder.append(String.format(" to version: %s", updatedVersion .getVersionNumber()));
        return titleBuilder.toString();
    }

    private String addDependenciesToTitle() {
        int i = 0;
        StringBuilder builder = new StringBuilder();
        for (Dependency dependency : dependencies) {
            i++;
            builder.append(String.format("%s:%s", dependency.getGroup(), dependency.getName()));
            if (i != dependencies.size()) {
                builder.append(" and ");
            }
        }
        return builder.toString();
    }

    public boolean isMultiBump() {
        return dependencies.size() > 1;
    }
}
