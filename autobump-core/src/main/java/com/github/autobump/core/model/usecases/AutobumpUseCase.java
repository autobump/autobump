package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.AutobumpResult;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.UseCaseConfiguration;
import com.github.autobump.core.model.Workspace;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.net.URI;
import java.util.Set;

@Data
@Builder
public class AutobumpUseCase {

    @NonNull
    private final URI uri;
    @NonNull
    private final UseCaseConfiguration config;

    public AutobumpResult doAutoBump() {
        Workspace workspace = config.getGitClient().clone(getUri());
        Set<Dependency> dependencies = config.getDependencyResolver().resolve(workspace);
        var combinedbumps = BumpResolverUseCase.builder()
                .dependencies(dependencies)
                .ignoreRepository(config.getIgnoreRepository())
                .versionRepository(config.getVersionRepository())
                .build()
                .doResolve();
        makeBumpsAndPullRequests(workspace, combinedbumps);
        return new AutobumpResult(combinedbumps.size());
    }

    private void makeBumpsAndPullRequests(Workspace workspace, Set<Bump> bumps) {
        for (Bump bump : bumps) {
            BumpUseCase.builder()
                    .dependencyBumper(config.getDependencyBumper())
                    .workspace(workspace)
                    .bump(bump)
                    .build()
                    .doBump();
            PullRequestUseCase.builder()
                    .uri(uri)
                    .gitProvider(config.getGitProvider())
                    .gitClient(config.getGitClient())
                    .urlHelper(config.getUrlHelper())
                    .workspace(workspace)
                    .bump(bump)
                    .build()
                    .doPullRequest();
        }
    }
}
