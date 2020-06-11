package com.github.autobump.core.model.gitproviders;

import com.github.autobump.core.model.UrlHelper;

public abstract class GitProviderUrlHelper extends UrlHelper {
    public abstract int getPullRequestId(String pullRequestUrl);
}
