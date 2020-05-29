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
        PullRequestResponse newPullRequest = makePullRequest(pullRequest);
        PullRequest pr = pullRequestThatShouldBeClosed(pullRequest);
        if (pr != null) {
            addCommentToDeclinedPullRequest(newPullRequest.getLink(), pr);
            gitClient.deleteBranch(workspace, pr.getBranchName());
            gitProvider.closePullRequest(pr);
        }
    }

    private PullRequest pullRequestThatShouldBeClosed(PullRequest newPullRequest){
        PullRequest pullRequest = null;
        for (PullRequest pr : getOpenPullRequests(newPullRequest.getRepoOwner(), newPullRequest.getRepoName())
        ) {
            var el1 = parseGroupAndArtifactId(newPullRequest.getTitle());
            var el2 = parseGroupAndArtifactId(pr.getTitle());
            var el3 = parseVersionNumber(newPullRequest.getTitle());
            var el4 = parseVersionNumber(pr.getTitle());
            if (el1.equals(el2)
                    && !el3.equals(el4)){
                pullRequest = pr;
            }
        }
        return pullRequest;
    }

    private void addCommentToDeclinedPullRequest(String link, PullRequest pr) {
        gitProvider.commentPullRequest(pr, "Autobump has superseded this PR by a new one: " + link);
    }

    private String parseGroupAndArtifactId(String title) {
        String[] elements = title.split(" ");
        return elements[1];
    }

    private String parseVersionNumber(String title) {
        String[] elements = title.split(" ");
        return elements[elements.length-1];
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
