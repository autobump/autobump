package com.github.autobump.core.model;

public abstract class GitProviderUrlHelper extends UrlHelper {
    public abstract int getPullRequestId(String pullRequestUrl);
}
