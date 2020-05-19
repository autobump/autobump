package com.github.autobump.bitbucket.model;

import com.github.autobump.bitbucket.model.dtos.PullRequestBodyDto;
import com.github.autobump.bitbucket.model.dtos.PullRequestResponseDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

interface BitBucketApi {
    @RequestLine("POST /repositories/{repoOwner}/{repoName}/pullrequests")
    @Headers("Content-Type: application/json")
    PullRequestResponseDto createPullRequest(@Param("repoOwner") String repoOwner,
                                             @Param("repoName") String repoName,
                                             PullRequestBodyDto pullRequestBodyDto);

    @RequestLine("GET /repositories/{repoOwner}/{repoName}/pullrequests")
    List<PullRequestResponseDto> getOpenPullRequests(@Param("repoOwner") String repoOwner,
                                                     @Param("repoName") String repoName);

    @RequestLine("POST /repositories/{repoOwner}/{repoName}/pullrequests/{pullRequestId}/decline")
    void closePullRequest(@Param("repoOwner") String repoOwner,
                          @Param("repoName") String repoName,
                          @Param("pullRequestId") String pullRequestId);
}

