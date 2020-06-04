package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.Workspace;
import lombok.Builder;

@Builder
public class BumpUseCase {
    private final DependencyBumper dependencyBumper;

    public void doBump(Workspace workspace, Bump bump) {
        dependencyBumper.bump(workspace, bump);
    }
}
