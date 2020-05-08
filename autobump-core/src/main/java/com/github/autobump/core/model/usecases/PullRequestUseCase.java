package com.github.autobump.core.model.usecases;

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
    private final String groupId;
    private final String version;
    private final URI uri;


    public PullRequestUseCase(GitProvider gitProvider,
                              GitClient gitClient,
                              UrlHelper urlHelper,
                              Workspace workspace,
                              String groupId,
                              String version,
                              URI uri) {
        this.gitProvider = gitProvider;
        this.gitClient = gitClient;
        this.urlHelper = urlHelper;
        this.workspace = workspace;
        this.groupId = groupId;
        this.version = version;
        this.uri = uri;
    }

    public void doPullRequest() {
        var commitResult =
                gitClient.commitToNewBranch(workspace, groupId, version);
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
