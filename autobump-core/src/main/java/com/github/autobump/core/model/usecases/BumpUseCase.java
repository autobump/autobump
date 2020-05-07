package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.Workspace;

public class BumpUseCase {
    private final DependencyBumper dependencyBumper;

    public BumpUseCase(DependencyBumper dependencyBumper) {
        this.dependencyBumper = dependencyBumper;
    }

    public Bump doBump(Workspace workspace, Dependency dependency, Version latestVersion) {
        Bump bump = new Bump(dependency, latestVersion);
        dependencyBumper.bump(workspace, bump);
        return bump;
    }

}
