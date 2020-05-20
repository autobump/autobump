package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.IgnoreRepository;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.UrlHelper;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.core.model.events.PushEvent;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

@Builder
public class RebaseUseCase {
    GitClient gitClient;
    GitProvider gitProvider;
    UrlHelper urlHelper;
    IgnoreRepository ignoreRepository;
    DependencyResolver dependencyResolver;
    DependencyBumper dependencyBumper;

    public void handlePushEvent(PushEvent event) {
        List<PullRequest> pullRequests = getOpenPullRequests(
                urlHelper.getOwnerName(event.getUri().toString()),
                urlHelper.getRepoName(event.getUri().toString()));
        Workspace workspace = gitClient.clone(event.getUri());
        for (PullRequest p : pullRequests) {
            boolean hasConflicts = gitClient.rebaseBranchFromMaster(workspace, p.getBranchName()).isHasConflicts();
            if (hasConflicts) {
                AutoBumpSingleGroupUseCase.builder()
                        .pullRequest(p)
                        .gitClient(gitClient)
                        .gitProvider(gitProvider)
                        .ignoreRepository(ignoreRepository)
                        .dependencyResolver(dependencyResolver)
                        .dependencyBumper(dependencyBumper)
                        .build()
                        .doSingleGroupAutoBump();
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
