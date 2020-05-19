package com.github.autobump.core.model;

import java.net.URI;

public interface GitClient {
    Workspace clone(URI uri);

    CommitResult commitToNewBranch(Workspace workspace, Bump bump);

    CommitResult commitToExistingBranch(Workspace workspace, Bump bump, String branchName);

    AutoBumpRebaseResult rebaseBranchFromMaster(Workspace workspace, String branchName);
}
