package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.GitProviderUrlHelper;
import com.github.autobump.core.model.PullRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.inject.Named;

import java.net.URI;

@Named
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class PostCommentOnPullRequestUseCase {

    @NonNull
    private final GitProvider gitProvider;
    @NonNull
    private final GitProviderUrlHelper urlHelper;

    public void postCommentOnPullRequest(@NonNull URI uri, int pullrequestId, @NonNull String commentContent) {
        gitProvider.commentPullRequest(PullRequest.builder()
                        .pullRequestId(pullrequestId)
                        .title("")
                        .branchName("")
                        .repoOwner(urlHelper.getOwnerName(uri.toString()))
                        .repoName(urlHelper.getRepoName(uri.toString()))
                        .build(),
                commentContent
        );
    }
}
