package com.github.autobump.core.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.net.URI;

@Value
@Builder
public class AutobumpUseCase {
    @NonNull
    GitProvider gitProvider;
    @NonNull
    GitClient gitClient;
    @NonNull
    DependencyResolver dependencyResolver;
    @NonNull
    VersionRepository versionRepository;
    @NonNull
    DependencyBumper dependencyBumper;
    @NonNull
    UrlHelper urlHelper;
    @NonNull
    URI uri;

    public AutobumpResult execute() {
        int amountOfBumps = 0;
        Workspace workspace = gitClient.clone(getUri());
        for (Dependency dependency : dependencyResolver.resolve(workspace)) {
            Version latestVersion = getLatestVersion(dependency);
            if (latestVersion != null && dependency.getVersion().compareTo(latestVersion) > 0) {
                Bump bump = doBump(workspace, dependency, latestVersion);
                makeAndExecutePullRequest(workspace, bump);
                amountOfBumps++;
            }
        }
        return new AutobumpResult(amountOfBumps);
    }

    private Version getLatestVersion(Dependency dependency) {
        return versionRepository.getAllAvailableVersions(dependency).stream()
                .sorted().findFirst().orElse(null);
    }

    private void doPullRequest(PullRequest pullRequest) {
        gitProvider.makePullRequest(pullRequest);
    }

    private void makeAndExecutePullRequest(Workspace workspace, Bump bump) {
        var commitResult = gitClient.commitToNewBranch(workspace, bump);
        PullRequest pullRequest = PullRequest.builder()
                .branchName(commitResult.getBranchName())
                .title(commitResult.getCommitMessage())
                .repoName(urlHelper.getRepoName(uri.toString()))
                .repoOwner(urlHelper.getOwnerName(uri.toString()))
                .build();
        doPullRequest(pullRequest);
    }

    private Bump doBump(Workspace workspace, Dependency dependency, Version latestVersion) {
        Bump bump = new Bump(dependency, latestVersion);
        dependencyBumper.bump(workspace, bump);
        return bump;
    }

}
