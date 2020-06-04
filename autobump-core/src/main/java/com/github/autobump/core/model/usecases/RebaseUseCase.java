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

    public void handlePushEvent(PushEvent event) {
        List<PullRequest> pullRequests = getOpenPullRequests(
                config.getGitProviderUrlHelper().getOwnerName(event.getGitUri().toString()),
                config.getGitProviderUrlHelper().getRepoName(event.getGitUri().toString()));
        if (!pullRequests.isEmpty()) {
            Workspace workspace = config.getGitClient().clone(event.getGitUri());
            for (PullRequest p : pullRequests) {
                if (config.getGitClient().rebaseBranchFromMaster(workspace, p.getBranchName()).isConflicted()) {
                    AutoBumpSingleGroupUseCase.builder()
                            .config(config)
                            .build()
                            .doSingleGroupAutoBump(event.getGitUri(), workspace, p);
                }
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
