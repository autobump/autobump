package com.github.autobump.core.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class UrlHelper {
    private static final Pattern OWNER_REPO_PATTERN =
            Pattern.compile("^https?:\\/\\/(.*@)?.+\\/(.*)\\/(.*)\\.?g?i?t?$");

    public String getOwnerName(String repositoryUrl) {
        Matcher matcher = OWNER_REPO_PATTERN.matcher(repositoryUrl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    String.format("Unable to extract owner name from %s", repositoryUrl));
        }
        return matcher.group(2);
    }

    public String getRepoName(String repositoryUrl) {
        Matcher matcher = OWNER_REPO_PATTERN.matcher(repositoryUrl);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    String.format("Unable to extract repository name from %s", repositoryUrl));
        }
        return matcher.group(3).replace(".git","");
    }
}
