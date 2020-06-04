package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.AutobumpResult;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.PullRequestResponse;
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
    private final GitClient gitClient;
    @NonNull
    private final DependencyResolver dependencyResolver;
    @NonNull
    private final BumpResolverUseCase bumpResolverUseCase;
    @NonNull
    private final FetchVersionReleaseNotesUseCase fetchVersionReleaseNotesUseCase;
    @NonNull
    private final PostCommentOnPullRequestUseCase postCommentOnPullRequestUseCase;
    @NonNull
    private final PullRequestUseCase pullRequestUseCase;
    @NonNull
    private final BumpUseCase bumpUseCase;

    public AutobumpResult doAutoBump(@NonNull URI uri) {
        Workspace workspace = gitClient.clone(uri);
        Set<Dependency> dependencies = dependencyResolver.resolve(workspace);
        var combinedbumps = bumpResolverUseCase.doResolve(dependencies);
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
            postCommentOnPullRequestUseCase.postCommentOnPullRequest(uri, pullRequestResponse.getId(), commentContent);
        }
    }

    private String fetchVersionReleaseNotes(Bump bump) {
        return fetchVersionReleaseNotesUseCase.fetchVersionReleaseNotes(bump);
    }

    private PullRequestResponse doPullRequest(URI uri, Workspace workspace, Bump bump) {
        return pullRequestUseCase.doPullRequest(workspace, uri, bump);
    }

    private void doBump(Workspace workspace, Bump bump) {
        bumpUseCase.doBump(workspace, bump);
    }
}
