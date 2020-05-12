package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.Workspace;
import lombok.Builder;

@Builder
public class BumpUseCase {
    private final DependencyBumper dependencyBumper;
    private final Workspace workspace;
    private final Bump bump;

    public BumpUseCase(DependencyBumper dependencyBumper,
                       Workspace workspace, Bump bump) {
        this.dependencyBumper = dependencyBumper;
        this.workspace = workspace;
        this.bump = bump;
    }

    public void doBump() {
        dependencyBumper.bump(workspace, bump);
    }

}
