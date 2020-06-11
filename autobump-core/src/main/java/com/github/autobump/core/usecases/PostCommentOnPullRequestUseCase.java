package com.github.autobump.core.usecases;

import com.github.autobump.core.model.UrlHelper;
import com.github.autobump.core.model.domain.PullRequest;
import com.github.autobump.core.model.gitproviders.GitProvider;
import lombok.Builder;
import lombok.NonNull;

import java.net.URI;

@Builder
public class PostCommentOnPullRequestUseCase {

    private final int pullrequestId;
    @NonNull
    private final GitProvider gitProvider;
    @NonNull
    private final URI uri;
    @NonNull
    private final UrlHelper urlHelper;
    @NonNull
    private final String commentContent;

    public void postCommentOnPullRequest() {
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
