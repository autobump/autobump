package com.github.autobump.springboot.services;

import com.github.autobump.core.model.AutoBumpRebaseResult;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.CommitResult;
import com.github.autobump.core.model.DeleteBranchResult;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.jgit.model.JGitGitClient;

import javax.inject.Inject;
import java.net.URI;

public class SpringbootJGitGitClient extends JGitGitClient {

    private final JwtFactory jwtFactory;

    @Inject
    public SpringbootJGitGitClient(JwtFactory jwtFactory) {
        super("x-token-auth", "");
        this.jwtFactory = jwtFactory;
    }

    @Override
    public Workspace clone(URI uri) {
        setPassword(jwtFactory.getAccesToken());
        return super.clone(uri);
    }

    @Override
    public CommitResult commitToNewBranch(Workspace workspace, Bump bump) {
        setPassword(jwtFactory.getAccesToken());
        return super.commitToNewBranch(workspace, bump);
    }

    @Override
    public CommitResult commitToExistingBranch(Workspace workspace, Bump bump, String branchName) {
        setPassword(jwtFactory.getAccesToken());
        return super.commitToExistingBranch(workspace, bump, branchName);
    }

    @Override
    public AutoBumpRebaseResult rebaseBranchFromMaster(Workspace workspace, String branchName) {
        setPassword(jwtFactory.getAccesToken());
        return super.rebaseBranchFromMaster(workspace, branchName);
    }

    @Override
    public DeleteBranchResult deleteBranch(Workspace workspace, String branchName) {
        setPassword(jwtFactory.getAccesToken());
        return super.deleteBranch(workspace, branchName);
    }
}
