package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.*;
import com.github.autobump.core.model.events.PushEvent;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public class RebaseUseCase {
    GitClient gitClient;
    GitProvider gitProvider;
    UrlHelper urlHelper;
    IgnoreRepository ignoreRepository;
    DependencyResolver dependencyResolver;
    DependencyBumper dependencyBumper;

    public void handlePushEvent(PushEvent event){
        List<String> branchNames = getOpenPullRequests(
                urlHelper.getOwnerName(event.getUri().toString()),
                urlHelper.getRepoName(event.getUri().toString()));
        for (String branchName: branchNames) {
            Workspace workspace = gitClient.clone(event.getUri());
            boolean hasConflicts = gitClient.rebaseBranchFromMaster(workspace, branchName).isHasConflicts();
            if (hasConflicts){
                String groupId = branchName.split("/")[1];
                AutoBumpSingleGroupUseCase.builder()
                        .branchName(branchName)
                        .groupId(groupId)
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

    private List<String> getOpenPullRequests(String repoOwner, String repoName) {
        List<String> branchNames = new ArrayList<>();
        List<PullRequest> pullRequests = gitProvider.getOpenPullRequests(repoOwner, repoName);
        for (PullRequest pr: pullRequests
             ) {
            if (pr.getTitle().startsWith("Bumped")){
                branchNames.add(pr.getBranchName());
            }
        }
        return branchNames;
    }
}
