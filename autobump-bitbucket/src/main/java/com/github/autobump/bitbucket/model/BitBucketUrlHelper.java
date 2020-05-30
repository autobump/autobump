package com.github.autobump.bitbucket.model;

import com.github.autobump.core.model.UrlHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BitBucketUrlHelper implements UrlHelper {
    private static final Pattern OWNER_REPO_PATTERN =
            Pattern.compile("^https?:\\/\\/(.*@)?.+\\/(.*)\\/(.*)\\.git$");
    private static final Pattern PULL_REQUEST_PATTERN =
            Pattern.compile("^https?:\\/\\/(.*@)?.+\\/(.*)\\/(.*)\\/(.*)\\/(.*)$");

    @Override
    public String getOwnerName(String repositoryUrl){
        Matcher matcher = OWNER_REPO_PATTERN.matcher(repositoryUrl);
        if (!matcher.matches()){
            throw new IllegalArgumentException(
                    String.format("Unable to extract owner name from %s", repositoryUrl));
        }
        return matcher.group(2);
    }

    @Override
    public String getRepoName(String repositoryUrl){
        Matcher matcher = OWNER_REPO_PATTERN.matcher(repositoryUrl);
        if (!matcher.matches()){
            throw new IllegalArgumentException(
                    String.format("Unable to extract repository name from %s", repositoryUrl));
        }
        return matcher.group(3);
    }

    @Override
    public int getPullRequestId(String pullRequestUrl){
        Matcher matcher = PULL_REQUEST_PATTERN.matcher(pullRequestUrl);
        if (!matcher.matches()){
            throw new IllegalArgumentException(
                    String.format("Unable to extract the pullrequest-id from %s", pullRequestUrl));
        }
        return Integer.parseInt(matcher.group(5));
    }
}
