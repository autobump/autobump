package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.UseCaseConfiguration;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.core.model.events.PushEvent;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public class RebaseUseCase {

    private final UseCaseConfiguration config;
    private final PushEvent event;

    public void handlePushEvent() {
        List<PullRequest> pullRequests = getOpenPullRequests(
                config.getGitProviderUrlHelper().getOwnerName(event.getUri().toString()),
                config.getGitProviderUrlHelper().getRepoName(event.getUri().toString()));
        Workspace workspace = config.getGitClient().clone(event.getUri());
        for (PullRequest p : pullRequests) {
            if (config.getGitClient().rebaseBranchFromMaster(workspace, p.getBranchName()).isConflicted()) {
                AutoBumpSingleGroupUseCase.builder()
                        .pullRequest(p)
                        .uri(event.getUri())
                        .config(config)
                        .workspace(workspace)
                        .build()
                        .doSingleGroupAutoBump();
            }
        }
    }

    private List<PullRequest> getOpenPullRequests(String repoOwner, String repoName) {
        return config.getGitProvider().getOpenPullRequests(repoOwner, repoName)
                .stream()
                .filter(p -> p.getTitle().startsWith("Bumped"))
                .collect(Collectors.toUnmodifiableList());
    }
}
