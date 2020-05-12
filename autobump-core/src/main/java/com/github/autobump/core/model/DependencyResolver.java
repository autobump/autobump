package com.github.autobump.core.model;

import java.util.Set;

public interface DependencyResolver {
    Set<Dependency> resolve(Workspace workspace);
    Set<Dependency> resolve(Workspace workspace, Set<Dependency> ignoredInternal);
    String getBuildFileName();
}
