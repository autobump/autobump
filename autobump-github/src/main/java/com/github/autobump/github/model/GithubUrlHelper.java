package com.github.autobump.github.model;

import com.github.autobump.core.model.UrlHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubUrlHelper implements UrlHelper {
    private static final Pattern OWNER_REPO_PATTERN =
            Pattern.compile("^https?://(.*@)?.+/(.*)/(.*)\\.git$");
    private static final Pattern PULL_REQUEST_PATTERN =
            Pattern.compile("^https?://(.*@)?.+/(.*)/(.*)/(.*)/(.*)$");

    @Override
    public String getOwnerName(String repositoryUrl) {
        Matcher matcher = OWNER_REPO_PATTERN.matcher(repositoryUrl);
        matcher.matches();
        return matcher.group(2);
    }

    @Override
    public String getRepoName(String repositoryUrl) {
        Matcher matcher = OWNER_REPO_PATTERN.matcher(repositoryUrl);
        matcher.matches();
        return matcher.group(3);
    }

    @Override
    public int getPullRequestId(String pullRequestUrl) {
        Matcher matcher = PULL_REQUEST_PATTERN.matcher(pullRequestUrl);
        matcher.matches();
        return Integer.parseInt(matcher.group(5));
    }
}
