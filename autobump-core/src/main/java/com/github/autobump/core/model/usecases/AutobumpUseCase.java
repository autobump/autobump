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
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.util.Set;

@Named
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AutobumpUseCase {
    @Inject
    private final GitClient gitClient;
    @Inject
    private final DependencyResolver dependencyResolver;
    @Inject
    private final BumpResolverUseCase bumpResolverUseCase;
    @Inject
    private final FetchVersionReleaseNotesUseCase fetchVersionReleaseNotesUseCase;
    @Inject
    private final PostCommentOnPullRequestUseCase postCommentOnPullRequestUseCase;
    @Inject
    private final PullRequestUseCase pullRequestUseCase;
    @Inject
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
