package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.core.model.events.PushEvent;

import java.util.ArrayList;
import java.util.List;

public class RebaseUseCase {
    GitClient gitClient;
    GitProvider gitProvider;

    public void handlePushEvent(PushEvent event){
       List<String> branchNames = getOpenPullRequests(event.getRepoOwner(), event.getRepoName());
       for (String branchName: branchNames) {
           Workspace workspace = gitClient.clone(event.getUri());
           gitClient.rebaseBranch(workspace, branchName);
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
