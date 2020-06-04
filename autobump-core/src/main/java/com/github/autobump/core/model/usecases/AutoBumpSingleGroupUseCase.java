package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.AutobumpResult;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.UseCaseConfiguration;
import com.github.autobump.core.model.Workspace;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
public class AutoBumpSingleGroupUseCase {

    @NonNull
    private final UseCaseConfiguration config;

    public AutobumpResult doSingleGroupAutoBump(
            @NonNull URI uri,
            @NonNull Workspace workspace,
            @NonNull PullRequest pullRequest
    ) {
        Set<Dependency> dependencies = config.getDependencyResolver().resolve(workspace)
                .stream().filter(d -> d.getGroup()
                        .equalsIgnoreCase(parseGroupName(pullRequest.getTitle())))
                .collect(Collectors.toUnmodifiableSet());
        var combinedbumps = BumpResolverUseCase.builder()
                .ignoreRepository(config.getIgnoreRepository())
                .versionRepository(config.getVersionRepository())
                .build()
                .doResolve(dependencies);
        if (combinedbumps.isEmpty()) {
            config.getGitProvider().closePullRequest(pullRequest);
            config.getGitClient().deleteBranch(workspace, pullRequest.getBranchName());
        }
        makeBumpsAndPush(workspace, combinedbumps, pullRequest.getBranchName());
        return new AutobumpResult(combinedbumps.size());
    }

    private void makeBumpsAndPush(Workspace workspace, Set<Bump> bumps, String branchName) {
        for (Bump bump : bumps) {
            BumpUseCase.builder()
                    .dependencyBumper(config.getDependencyBumper())
                    .build()
                    .doBump(workspace, bump);
            PushUseCase.builder().gitClient(config.getGitClient())
                    .build()
                    .doPush(workspace, bump, branchName);
        }
    }

    private String parseGroupName(String title) {
        String[] elements = title.split(" ");
        return elements[1].split(":")[0];
    }
}
