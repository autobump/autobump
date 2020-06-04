package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.GitProviderUrlHelper;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.core.model.events.PushEvent;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

@Named
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RebaseUseCase {

    private final AutoBumpSingleGroupUseCase autoBumpSingleGroupUseCase;
    private final GitClient gitClient;
    private final GitProvider gitProvider;
    private final GitProviderUrlHelper gitProviderUrlHelper;

    public void handlePushEvent(PushEvent event) {
        List<PullRequest> pullRequests = getOpenPullRequests(
                gitProviderUrlHelper.getOwnerName(event.getGitUri().toString()),
                gitProviderUrlHelper.getRepoName(event.getGitUri().toString()));
        if (!pullRequests.isEmpty()) {
            Workspace workspace = gitClient.clone(event.getGitUri());
            for (PullRequest p : pullRequests) {
                if (gitClient.rebaseBranchFromMaster(workspace, p.getBranchName()).isConflicted()) {
                    autoBumpSingleGroupUseCase
                            .doSingleGroupAutoBump(workspace, p);
                }
            }
        }
    }

    private List<PullRequest> getOpenPullRequests(String repoOwner, String repoName) {
        return gitProvider.getOpenPullRequests(repoOwner, repoName)
                .stream()
                .filter(p -> p.getTitle().startsWith("Bumped"))
                .collect(Collectors.toUnmodifiableList());
    }
}
