package model;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

interface BitBucketApi {
    @RequestLine("POST /repositories/{repoOwner}/{repoName}/pullrequests")
    @Headers("Content-Type: application/json")
    void createPullRequest(@Param("repoOwner") String repoOwner,
                           @Param("repoName") String repoName,
                           PullRequestBody pullRequestBody);
}
