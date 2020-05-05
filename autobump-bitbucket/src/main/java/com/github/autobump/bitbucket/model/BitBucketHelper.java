package com.github.autobump.bitbucket.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BitBucketHelper {
    private static final Pattern OWNER_REPO_PATTERN = Pattern.compile("https?://(.+@)?bitbucket.org/(.+)/(.+).git");

    public static String getOwnerName(String repositoryUrl){
        Matcher matcher = OWNER_REPO_PATTERN.matcher(repositoryUrl);
        return matcher.group(2);
    }
    public static String getRepoName(String repositoryUrl){
        Matcher matcher = OWNER_REPO_PATTERN.matcher(repositoryUrl);
        return matcher.group(3);
    }
}
