package com.github.autobump.bitbucket.model;

import com.github.autobump.bitbucket.model.dtos.PullRequestBodyDto;
import com.github.autobump.bitbucket.model.dtos.PullRequestListDto;
import com.github.autobump.bitbucket.model.dtos.PullRequestResponseDto;
import com.github.autobump.core.model.PullRequest;
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
    PullRequestListDto getOpenPullRequests(@Param("repoOwner") String repoOwner,
                                           @Param("repoName") String repoName);
}
