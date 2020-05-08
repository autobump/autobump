package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.UrlHelper;
import com.github.autobump.core.model.Workspace;
import lombok.Builder;

import java.net.URI;

@Builder
public class PullRequestUseCase {
    private final GitProvider gitProvider;
    private final GitClient gitClient;
    private final UrlHelper urlHelper;
    private final Workspace workspace;
    private final Bump bump;
    private final URI uri;


    public PullRequestUseCase(GitProvider gitProvider,
                              GitClient gitClient,
                              UrlHelper urlHelper,
                              Workspace workspace,
                              Bump bump,
                              URI uri) {
        this.gitProvider = gitProvider;
        this.gitClient = gitClient;
        this.urlHelper = urlHelper;
        this.workspace = workspace;
        this.bump = bump;
        this.uri = uri;
    }

    public void doPullRequest() {
        var commitResult = gitClient.commitToNewBranch(workspace, bump);
        PullRequest pullRequest = PullRequest.builder()
                .branchName(commitResult.getBranchName())
                .title(commitResult.getCommitMessage())
                .repoName(urlHelper.getRepoName(uri.toString()))
                .repoOwner(urlHelper.getOwnerName(uri.toString()))
                .build();
        makePullRequest(pullRequest);
    }

    private void makePullRequest(PullRequest pullRequest) {
        gitProvider.makePullRequest(pullRequest);
    }
}
