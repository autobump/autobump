package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.AutobumpResult;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.PullRequestResponse;
import com.github.autobump.core.model.ReleaseNotesSource;
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
    private final UseCaseConfiguration config;
    @NonNull
    private final ReleaseNotesSource releaseNotesSource;

    public AutobumpResult doAutoBump(@NonNull URI uri) {
        Workspace workspace = config.getGitClient().clone(uri);
        Set<Dependency> dependencies = config.getDependencyResolver().resolve(workspace);
        var combinedbumps = BumpResolverUseCase.builder()
                .ignoreRepository(config.getIgnoreRepository())
                .versionRepository(config.getVersionRepository())
                .build()
                .doResolve(dependencies);
        makeBumpsAndPullRequests(uri, workspace, combinedbumps);
        return new AutobumpResult(combinedbumps.size());
    }

    private void makeBumpsAndPullRequests(URI uri, Workspace workspace, Set<Bump> bumps) {
        for (Bump bump : bumps) {
            doBump(workspace, bump);
            PullRequestResponse pullRequestResponse = doPullRequest(uri, workspace, bump);
            String commentContent = fetchVersionReleaseNotes(bump);
            postCommentOnPullRequest(uri, pullRequestResponse, commentContent);
        }
    }

    private void postCommentOnPullRequest(URI uri, PullRequestResponse pullRequestResponse, String commentContent) {
        if (!commentContent.isBlank()) {
            PostCommentOnPullRequestUseCase.builder()
                    .gitProvider(config.getGitProvider())
                    .urlHelper(config.getGitProviderUrlHelper())
                    .build()
                    .postCommentOnPullRequest(uri, pullRequestResponse.getId(), commentContent);
        }
    }

    private String fetchVersionReleaseNotes(Bump bump) {
        return FetchVersionReleaseNotesUseCase.builder()
                .releaseNotesSource(releaseNotesSource)
                .versionRepository(config.getVersionRepository())
                .build()
                .fetchVersionReleaseNotes(bump);
    }

    private PullRequestResponse doPullRequest(URI uri, Workspace workspace, Bump bump) {
        return PullRequestUseCase.builder()
                .gitProvider(config.getGitProvider())
                .gitClient(config.getGitClient())
                .gitProviderUrlHelper(config.getGitProviderUrlHelper())
                .build()
                .doPullRequest(workspace, uri, bump);
    }

    private void doBump(Workspace workspace, Bump bump) {
        BumpUseCase.builder()
                .dependencyBumper(config.getDependencyBumper())
                .build()
                .doBump(workspace, bump);
    }
}
