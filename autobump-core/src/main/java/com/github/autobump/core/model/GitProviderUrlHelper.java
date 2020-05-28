package com.github.autobump.core.model;

public interface GitProviderUrlHelper extends UrlHelper {
    int getPullRequestId(String pullRequestUrl);
}
