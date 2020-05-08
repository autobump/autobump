package com.github.autobump.bitbucket.model;

import com.github.autobump.core.model.UrlHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BitBucketUrlHelper implements UrlHelper {
    private static final Pattern OWNER_REPO_PATTERN =
            Pattern.compile("^https?:\\/\\/(.*@)?.+\\/(.*)\\/(.*)\\.git$");
    @Override
    public String getOwnerName(String repositoryUrl){
        Matcher matcher = OWNER_REPO_PATTERN.matcher(repositoryUrl);
        matcher.matches();
        return matcher.group(2);
    }

    @Override
    public String getRepoName(String repositoryUrl){
        Matcher matcher = OWNER_REPO_PATTERN.matcher(repositoryUrl);
        matcher.matches();
        return matcher.group(3);
    }
}
