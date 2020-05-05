package model;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import model.dtos.PullRequestBodyDto;
import model.dtos.PullRequestResponseDto;

interface BitBucketApi {
    @RequestLine("POST /repositories/{repoOwner}/{repoName}/pullrequests")
    @Headers("Content-Type: application/json")
    PullRequestResponseDto createPullRequest(@Param("repoOwner") String repoOwner,
                                             @Param("repoName") String repoName,
                                             PullRequestBodyDto pullRequestBodyDto);
}
