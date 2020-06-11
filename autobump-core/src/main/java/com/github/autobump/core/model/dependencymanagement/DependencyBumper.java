package com.github.autobump.core.model.dependencymanagement;

import com.github.autobump.core.model.domain.Bump;
import com.github.autobump.core.model.domain.Workspace;

public interface DependencyBumper {
    void bump(Workspace workspace, Bump bump);
}
