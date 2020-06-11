package com.github.autobump.core.model.gitclients;

import com.github.autobump.core.model.domain.Bump;
import com.github.autobump.core.model.domain.Workspace;
import com.github.autobump.core.model.results.AutoBumpRebaseResult;
import com.github.autobump.core.model.results.CommitResult;
import com.github.autobump.core.model.results.DeleteBranchResult;

import java.net.URI;

public interface GitClient {
    Workspace clone(URI uri);

    CommitResult commitToNewBranch(Workspace workspace, Bump bump);

    CommitResult commitToExistingBranch(Workspace workspace, Bump bump, String branchName);

    AutoBumpRebaseResult rebaseBranchFromMaster(Workspace workspace, String branchName);

    DeleteBranchResult deleteBranch(Workspace workspace, String branchName);
}
