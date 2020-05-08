package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.Workspace;
import lombok.Builder;

@Builder
public class BumpUseCase {
    private final DependencyBumper dependencyBumper;
    private final Workspace workspace;
    private final Dependency dependency;
    private final Version latestVersion;

    public BumpUseCase(DependencyBumper dependencyBumper,
                       Workspace workspace,
                       Dependency dependency,
                       Version latestVersion) {
        this.dependencyBumper = dependencyBumper;
        this.dependency = dependency;
        this.latestVersion = latestVersion;
        this.workspace = workspace;
    }

    public Bump doBump() {
        Bump bump = new Bump(dependency, latestVersion);
        dependencyBumper.bump(workspace, bump);
        return bump;
    }

}
