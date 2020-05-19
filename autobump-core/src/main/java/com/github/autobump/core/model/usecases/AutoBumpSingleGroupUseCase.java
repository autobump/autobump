package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.*;
import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class AutoBumpSingleGroupUseCase{
    @NonNull
    private final GitProvider gitProvider;
    @NonNull
    private final GitClient gitClient;
    @NonNull
    private final DependencyResolver dependencyResolver;
    @NonNull
    private final VersionRepository versionRepository;
    @NonNull
    private final DependencyBumper dependencyBumper;
    @NonNull
    private final UrlHelper urlHelper;
    @NonNull
    private final URI uri;
    @NonNull
    private final IgnoreRepository ignoreRepository;
    @NotNull
    private final String groupId;
    @NotNull
    private final String branchName;

    @Builder
    public AutoBumpSingleGroupUseCase(GitProvider gitProvider,
                                      GitClient gitClient,
                                      DependencyResolver dependencyResolver,
                                      VersionRepository versionRepository,
                                      DependencyBumper dependencyBumper,
                                      UrlHelper urlHelper,
                                      IgnoreRepository ignoreRepository,
                                      URI uri,
                                      String groupId,
                                      String branchName) {
        this.gitProvider = gitProvider;
        this.gitClient = gitClient;
        this.dependencyResolver = dependencyResolver;
        this.versionRepository = versionRepository;
        this.dependencyBumper = dependencyBumper;
        this.urlHelper = urlHelper;
        this.ignoreRepository = ignoreRepository;
        this.uri = uri;
        this.groupId = groupId;
        this.branchName = branchName;
    }

    public AutobumpResult doSingleGroupAutoBump() {
        Workspace workspace = gitClient.clone(getUri());
        Set<Dependency> dependencies = dependencyResolver.resolve(workspace)
                .stream().filter(d -> d.getGroup().equalsIgnoreCase(groupId))
                .collect(Collectors.toUnmodifiableSet());
        if(dependencies.isEmpty()){
            String repoName = urlHelper.getRepoName(uri.toString());
            String repoOwner = urlHelper.getOwnerName(uri.toString());

            gitProvider.closePullRequest();
        }
        var combinedbumps = BumpResolverUseCase.builder()
                .dependencies(dependencies)
                .ignoreRepository(ignoreRepository)
                .versionRepository(versionRepository)
                .build()
                .doResolve();
        makeBumpsAndPush(workspace, combinedbumps, branchName);
        return new AutobumpResult(combinedbumps.size());
    }

    private void makeBumpsAndPush(Workspace workspace, Set<Bump> bumps, String branchName) {
        for (Bump bump : bumps) {
            BumpUseCase.builder()
                    .dependencyBumper(dependencyBumper)
                    .workspace(workspace)
                    .bump(bump)
                    .build()
                    .doBump();
            PushUseCase.builder().gitClient(gitClient)
                    .build()
                    .doPush(workspace, bump, branchName);
        }
    }
}
