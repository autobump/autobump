package com.github.autobump.core.usecases;

import com.github.autobump.core.model.domain.Bump;
import com.github.autobump.core.model.domain.Dependency;
import com.github.autobump.core.model.domain.Workspace;
import com.github.autobump.core.model.releasenotes.ReleaseNotesSource;
import com.github.autobump.core.model.results.AutobumpResult;
import com.github.autobump.core.model.results.PullRequestResult;
import com.github.autobump.core.repositories.SettingsRepository;
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
    @NonNull
    private final ReleaseNotesSource releaseNotesSource;

    private final SettingsRepository settingsRepository;

    public AutobumpResult doAutoBump() {
        Workspace workspace = config.getGitClient().clone(getUri());
        Set<Dependency> dependencies = config.getDependencyResolver().resolve(workspace);
        var combinedbumps = BumpResolverUseCase.builder()
                .dependencies(dependencies)
                .ignoreRepository(config.getIgnoreRepository())
                .settingsRepository(settingsRepository)
                .versionRepository(config.getVersionRepository())
                .build()
                .doResolve(config.getGitProviderUrlHelper().getRepoName(uri.toString()));
        makeBumpsAndPullRequests(workspace, combinedbumps);
        return new AutobumpResult(combinedbumps.size());
    }

    private void makeBumpsAndPullRequests(Workspace workspace, Set<Bump> bumps) {
        for (Bump bump : bumps) {
            doBump(workspace, bump);
            PullRequestResult pullRequestResult = doPullRequest(workspace, bump);
            String commentContent = fetchVersionReleaseNotes(bump);
            postCommentOnPullRequest(pullRequestResult, commentContent);
        }
    }

    private void postCommentOnPullRequest(PullRequestResult pullRequestResult, String commentContent) {
        if (!commentContent.isBlank() && pullRequestResult.getCommentCount() == 0) {
            PostCommentOnPullRequestUseCase.builder()
                    .gitProvider(config.getGitProvider())
                    .urlHelper(config.getGitProviderUrlHelper())
                    .uri(getUri())
                    .pullrequestId(pullRequestResult.getId())
                    .commentContent(commentContent)
                    .build()
                    .postCommentOnPullRequest();
        }
    }

    private String fetchVersionReleaseNotes(Bump bump) {
        return FetchVersionReleaseNotesUseCase.builder()
                .bump(bump)
                .releaseNotesSource(releaseNotesSource)
                .versionRepository(config.getVersionRepository())
                .build()
                .fetchVersionReleaseNotes();
    }

    private PullRequestResult doPullRequest(Workspace workspace, Bump bump) {
        return PullRequestUseCase.builder()
                .uri(uri)
                .gitProvider(config.getGitProvider())
                .gitClient(config.getGitClient())
                .gitProviderUrlHelper(config.getGitProviderUrlHelper())
                .settingsRepository(settingsRepository)
                .workspace(workspace)
                .bump(bump)
                .build()
                .doPullRequest();
    }

    private void doBump(Workspace workspace, Bump bump) {
        BumpUseCase.builder()
                .dependencyBumper(config.getDependencyBumper())
                .workspace(workspace)
                .bump(bump)
                .build()
                .doBump();
    }
}
