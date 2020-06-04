package com.github.autobump.bitbucket.model;

import com.github.autobump.core.model.GitProviderUrlHelper;

import javax.inject.Named;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Named
public class BitBucketGitProviderUrlHelper extends GitProviderUrlHelper {
    private static final Pattern PULL_REQUEST_PATTERN =
            Pattern.compile("^https?:\\/\\/(.*@)?.+\\/(.*)\\/(.*)\\/(.*)\\/(.*)$");

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
