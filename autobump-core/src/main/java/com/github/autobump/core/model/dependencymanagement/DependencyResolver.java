package com.github.autobump.core.model.dependencymanagement;

import com.github.autobump.core.model.domain.Dependency;
import com.github.autobump.core.model.domain.Workspace;

import java.util.Set;

public interface DependencyResolver {
    Set<Dependency> resolve(Workspace workspace);
    Set<Dependency> resolve(Workspace workspace, Set<Dependency> ignoredInternal);
}
