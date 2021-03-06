package com.github.autobump.bitbucket.model;

import com.github.autobump.bitbucket.model.dtos.CommentDto;
import com.github.autobump.bitbucket.model.dtos.MembersResponseDto;
import com.github.autobump.bitbucket.model.dtos.PullRequestBodyDto;
import com.github.autobump.bitbucket.model.dtos.PullRequestListDto;
import com.github.autobump.bitbucket.model.dtos.PullRequestResponseDto;
import com.github.autobump.bitbucket.model.dtos.RepositoryResponseDto;
import com.github.autobump.bitbucket.model.dtos.UserDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

interface BitBucketApi {
    @RequestLine("POST /repositories/{repoOwner}/{repoName}/pullrequests")
    @Headers("Content-Type: application/json")
    PullRequestResponseDto createPullRequest(@Param("repoOwner") String repoOwner,
                                             @Param("repoName") String repoName,
                                             PullRequestBodyDto pullRequestBodyDto);

    @RequestLine("GET /repositories/{repoOwner}/{repoName}/pullrequests")
    PullRequestListDto getOpenPullRequests(@Param("repoOwner") String repoOwner,
                                           @Param("repoName") String repoName);

    @RequestLine("POST /repositories/{repoOwner}/{repoName}/pullrequests/{pullRequestId}/decline")
    void closePullRequest(@Param("repoOwner") String repoOwner,
                          @Param("repoName") String repoName,
                          @Param("pullRequestId") String pullRequestId);

    @SuppressWarnings({"checkstyle:parameternumber"})
    @RequestLine("POST /repositories/{repoOwner}/{repoName}/pullrequests/{pullRequestId}/comments")
    @Headers("Content-Type: application/json")
    void commentPullRequest(@Param("repoOwner") String repoOwner,
                            @Param("repoName") String repoName,
                            @Param("pullRequestId") String pullRequestId,
                            CommentDto commentDto);

    @RequestLine("GET /repositories?role=owner")
    RepositoryResponseDto getRepos();

    @RequestLine("GET /workspaces/{workspace}/members")
    MembersResponseDto getMembersFromWorkspace(@Param("workspace") String workspace);

    @RequestLine("GET /user")
    UserDto getCurrentUserUuid();
}
