package com.github.autobump.core.model;

import com.github.autobump.jgit.model.CommitResult;

import java.net.URI;

public interface GitClient {
    Workspace clone(URI uri);

    CommitResult commitToNewBranch(Workspace workspace, Bump bump);
}
