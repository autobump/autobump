package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.PullRequestResponse;
import com.github.autobump.core.model.UrlHelper;
import com.github.autobump.core.model.Workspace;
import lombok.Builder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public class PullRequestUseCase {
    private final GitProvider gitProvider;
    private final GitClient gitClient;
    private final UrlHelper urlHelper;
    private final Workspace workspace;
    private final URI uri;
    private final Bump bump;


    public PullRequestUseCase(GitProvider gitProvider,
                              GitClient gitClient,
                              UrlHelper urlHelper,
                              Workspace workspace,
                              URI uri, Bump bump) {
        this.gitProvider = gitProvider;
        this.gitClient = gitClient;
        this.urlHelper = urlHelper;
        this.workspace = workspace;
        this.uri = uri;
        this.bump = bump;
    }

    public void doPullRequest() {
        var commitResult =
                gitClient.commitToNewBranch(workspace, bump);
        PullRequest pullRequest = PullRequest.builder()
                .branchName(commitResult.getBranchName())
                .title(bump.getTitle())
                .repoName(urlHelper.getRepoName(uri.toString()))
                .repoOwner(urlHelper.getOwnerName(uri.toString()))
                .build();
        PullRequest closedPullRequest = closeOpenPullRequest(pullRequest);
        PullRequestResponse newPullRequest = makePullRequest(pullRequest);
        if (closedPullRequest != null) {
            addCommentToDeclinedPullRequest(newPullRequest.getLink(), closedPullRequest);
        }
    }

    private PullRequest closeOpenPullRequest(PullRequest newPullrequest) {
        List<PullRequest> openPullrequests =
                getOpenPullRequests(newPullrequest.getRepoName(), newPullrequest.getRepoOwner());
        PullRequest pullRequest = null;
        for (PullRequest pr : openPullrequests
        ) {
            if (parseGroupNameAndArtifactId(newPullrequest.getTitle())
                    .equals(parseGroupNameAndArtifactId(pr.getTitle()))) {
                gitProvider.closePullRequest(pr);
                pullRequest = pr;
            }
        }
        return pullRequest;
    }

    private void addCommentToDeclinedPullRequest(String link, PullRequest pr) {
        gitProvider.commentPullRequest(pr, "This PR has been superseded by a new PR: " + link);
    }

    private String parseGroupNameAndArtifactId(String title) {
        String[] elements = title.split(" ");
        return elements[1];
    }

    private List<PullRequest> getOpenPullRequests(String repoOwner, String repoName) {
        return gitProvider.getOpenPullRequests(repoOwner, repoName)
                .stream()
                .filter(p -> p.getTitle().startsWith("Bumped"))
                .collect(Collectors.toUnmodifiableList());
    }

    private PullRequestResponse makePullRequest(PullRequest pullRequest) {
        return gitProvider.makePullRequest(pullRequest);
    }
}
