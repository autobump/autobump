package com.github.autobump.core.model;

public interface UrlHelper {
    String getOwnerName(String repositoryUrl);
    String getRepoName(String repositoryUrl);

    int getPullRequestId(String pullRequestUrl);
}
