package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.AutobumpResult;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.IgnoreRepository;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.UrlHelper;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.core.model.Workspace;
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
    @NonNull
    private final PullRequest pullRequest;

    @Builder
    public AutoBumpSingleGroupUseCase(GitProvider gitProvider,
                                      GitClient gitClient,
                                      DependencyResolver dependencyResolver,
                                      VersionRepository versionRepository,
                                      DependencyBumper dependencyBumper,
                                      UrlHelper urlHelper,
                                      IgnoreRepository ignoreRepository,
                                      URI uri,
                                      PullRequest pullRequest) {
        this.gitProvider = gitProvider;
        this.gitClient = gitClient;
        this.dependencyResolver = dependencyResolver;
        this.versionRepository = versionRepository;
        this.dependencyBumper = dependencyBumper;
        this.urlHelper = urlHelper;
        this.ignoreRepository = ignoreRepository;
        this.uri = uri;
        this.pullRequest = pullRequest;
    }

    public AutobumpResult doSingleGroupAutoBump() {
        Workspace workspace = gitClient.clone(getUri());
        Set<Dependency> dependencies = dependencyResolver.resolve(workspace)
                .stream().filter(d -> d.getGroup()
                        .equalsIgnoreCase(parseGroupName(pullRequest.getTitle())))
                .collect(Collectors.toUnmodifiableSet());
        if(dependencies.isEmpty()){
            gitProvider.closePullRequest(pullRequest);
        }
        var combinedbumps = BumpResolverUseCase.builder()
                .dependencies(dependencies)
                .ignoreRepository(ignoreRepository)
                .versionRepository(versionRepository)
                .build()
                .doResolve();
        makeBumpsAndPush(workspace, combinedbumps, pullRequest.getBranchName());
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

    private String parseGroupName(String title) {
        String[] elements = title.split(" ");
        return elements[1].split(":")[0];
    }

}
