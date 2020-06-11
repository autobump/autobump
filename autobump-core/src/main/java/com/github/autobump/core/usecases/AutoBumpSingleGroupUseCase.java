package com.github.autobump.core.usecases;

import com.github.autobump.core.model.domain.Bump;
import com.github.autobump.core.model.domain.Dependency;
import com.github.autobump.core.model.domain.PullRequest;
import com.github.autobump.core.model.domain.Workspace;
import com.github.autobump.core.model.results.AutobumpResult;
import com.github.autobump.core.repositories.SettingsRepository;
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
    @NonNull
    private final URI uri;
    @NonNull
    private final PullRequest pullRequest;
    @NonNull
    private final Workspace workspace;
    private final SettingsRepository settingsRepository;

    public AutobumpResult doSingleGroupAutoBump() {
        Set<Dependency> dependencies = config.getDependencyResolver().resolve(workspace)
                .stream().filter(d -> d.getGroup()
                        .equalsIgnoreCase(parseGroupName(pullRequest.getTitle())))
                .collect(Collectors.toUnmodifiableSet());
        var combinedbumps = BumpResolverUseCase.builder()
                .dependencies(dependencies)
                .ignoreRepository(config.getIgnoreRepository())
                .versionRepository(config.getVersionRepository())
                .settingsRepository(settingsRepository)
                .build()
                .doResolve(config.getGitProviderUrlHelper().getRepoName(uri.toString()));
        if (combinedbumps.isEmpty()) {
            config.getGitProvider().closePullRequest(pullRequest);
            config.getGitClient().deleteBranch(workspace, pullRequest.getBranchName());
        }
        makeBumpsAndPush(combinedbumps, pullRequest.getBranchName());
        return new AutobumpResult(combinedbumps.size());
    }

    private void makeBumpsAndPush(Set<Bump> bumps, String branchName) {
        for (Bump bump : bumps) {
            BumpUseCase.builder()
                    .dependencyBumper(config.getDependencyBumper())
                    .workspace(workspace)
                    .bump(bump)
                    .build()
                    .doBump();
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
