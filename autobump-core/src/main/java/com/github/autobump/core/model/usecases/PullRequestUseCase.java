package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.GitProviderUrlHelper;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.Workspace;
import lombok.Builder;

import java.net.URI;

@Builder
public class PullRequestUseCase {
    private final GitProvider gitProvider;
    private final GitClient gitClient;
    private final GitProviderUrlHelper gitProviderUrlHelper;
    private final Workspace workspace;
    private final URI uri;
    private final Bump bump;

    public void doPullRequest() {
        var commitResult =
                gitClient.commitToNewBranch(workspace, bump);
        PullRequest pullRequest = PullRequest.builder()
                .branchName(commitResult.getBranchName())
                .title(bump.getTitle())
                .repoName(gitProviderUrlHelper.getRepoName(uri.toString()))
                .repoOwner(gitProviderUrlHelper.getOwnerName(uri.toString()))
                .build();
        makePullRequest(pullRequest);
    }

    private void makePullRequest(PullRequest pullRequest) {
        gitProvider.makePullRequest(pullRequest);
    }
}
