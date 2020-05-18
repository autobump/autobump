package com.github.autobump.core.model;

import java.net.URI;

public interface GitClient {
    Workspace clone(URI uri);

    CommitResult commitToNewBranch(Workspace workspace, Bump bump);

    void rebaseBranch(Workspace workspace, String branchName);
}
