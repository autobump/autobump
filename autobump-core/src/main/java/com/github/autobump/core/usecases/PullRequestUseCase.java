package com.github.autobump.core.usecases;

import com.github.autobump.core.model.domain.Bump;
import com.github.autobump.core.model.domain.PullRequest;
import com.github.autobump.core.model.domain.Setting;
import com.github.autobump.core.model.domain.Workspace;
import com.github.autobump.core.model.gitclients.GitClient;
import com.github.autobump.core.model.gitproviders.GitProvider;
import com.github.autobump.core.model.gitproviders.GitProviderUrlHelper;
import com.github.autobump.core.model.results.CommitResult;
import com.github.autobump.core.model.results.PullRequestResult;
import com.github.autobump.core.repositories.SettingsRepository;
import lombok.Builder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public class PullRequestUseCase {
    private final GitProvider gitProvider;
    private final GitClient gitClient;
    private final GitProviderUrlHelper gitProviderUrlHelper;
    private final SettingsRepository settingsRepository;
    private final Workspace workspace;
    private final URI uri;
    private final Bump bump;

    public PullRequestResult doPullRequest() {
        var commitResult =
                gitClient.commitToNewBranch(workspace, bump);
        PullRequest pullRequest = getPullRequest(commitResult);
        PullRequestResult response = pushPullRequest(pullRequest);
        PullRequest pr = getPullRequestThatShouldBeSuperSeded(pullRequest, response.getId());
        if (pr != null) {
            addCommentToDeclinedPullRequest(response.getLink(), pr);
            gitClient.deleteBranch(workspace, pr.getBranchName());
            gitProvider.closePullRequest(pr);
        }
        return response;
    }

    private PullRequest getPullRequest(CommitResult commitResult) {
        PullRequest.PullRequestBuilder pr = PullRequest.builder()
                .branchName(commitResult.getBranchName())
                .title(bump.getTitle())
                .repoName(gitProviderUrlHelper.getRepoName(uri.toString()))
                .repoOwner(gitProviderUrlHelper.getOwnerName(uri.toString()));
        Setting settingForReviewer = settingsRepository
                .findSettingForReviewer(gitProviderUrlHelper.getRepoName(uri.toString()));
        if (settingForReviewer != null){
            pr.reviewer(settingForReviewer.getValue());
        }
        return pr.build();
    }

    private PullRequest getPullRequestThatShouldBeSuperSeded(PullRequest newPullRequest, int newPullrequestId){
        return getOpenPullRequests(newPullRequest.getRepoOwner(), newPullRequest.getRepoName())
                .stream()
                .filter(p -> p.getPullRequestId() != newPullrequestId
                        && shouldSupersede(newPullRequest, p))
                .findFirst()
                .orElse(null);
    }

    private boolean shouldSupersede(PullRequest newPullRequest, PullRequest pr) {
        return parseGroupAndArtifactId(newPullRequest.getTitle()).equals(parseGroupAndArtifactId(pr.getTitle()));
    }

    private void addCommentToDeclinedPullRequest(String newPrLink, PullRequest pr) {
        gitProvider.commentPullRequest(pr, "Autobump has superseded this pull request by a new one: " + newPrLink);
    }

    private String parseGroupAndArtifactId(String title) {
        String[] elements = title.split(" ");
        return elements[1];
    }

    private List<PullRequest> getOpenPullRequests(String repoOwner, String repoName) {
        return gitProvider.getOpenPullRequests(repoOwner, repoName)
                .stream()
                .filter(p -> p.getTitle().startsWith("Bumped"))
                .collect(Collectors.toUnmodifiableList());
    }

    private PullRequestResult pushPullRequest(PullRequest pullRequest) {
        return gitProvider.makePullRequest(pullRequest);
    }
}
