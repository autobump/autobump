package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.AutobumpResult;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.Workspace;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
public class AutoBumpSingleGroupUseCase {

    private final DependencyResolver dependencyResolver;
    private final GitClient gitClient;
    private final GitProvider gitProvider;

    private final BumpResolverUseCase bumpResolverUseCase;
    private final BumpUseCase bumpUseCase;
    private final PushUseCase pushUseCase;

    public AutobumpResult doSingleGroupAutoBump(
            @NonNull Workspace workspace,
            @NonNull PullRequest pullRequest
    ) {
        Set<Dependency> dependencies = dependencyResolver.resolve(workspace)
                .stream().filter(d -> d.getGroup()
                        .equalsIgnoreCase(parseGroupName(pullRequest.getTitle())))
                .collect(Collectors.toUnmodifiableSet());
        var combinedbumps = bumpResolverUseCase.doResolve(dependencies);
        if (combinedbumps.isEmpty()) {
            getGitProvider().closePullRequest(pullRequest);
            getGitClient().deleteBranch(workspace, pullRequest.getBranchName());
        }
        makeBumpsAndPush(workspace, combinedbumps, pullRequest.getBranchName());
        return new AutobumpResult(combinedbumps.size());
    }

    private void makeBumpsAndPush(Workspace workspace, Set<Bump> bumps, String branchName) {
        for (Bump bump : bumps) {
            bumpUseCase.doBump(workspace, bump);
            pushUseCase.doPush(workspace, bump, branchName);
        }
    }

    private String parseGroupName(String title) {
        String[] elements = title.split(" ");
        return elements[1].split(":")[0];
    }
}
