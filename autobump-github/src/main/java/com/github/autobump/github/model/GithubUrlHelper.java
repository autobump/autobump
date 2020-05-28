package com.github.autobump.github.model;

import com.github.autobump.core.model.ReleaseNotesUrlHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubUrlHelper implements ReleaseNotesUrlHelper {
    private static final Pattern OWNER_REPO_PATTERN =
            Pattern.compile("^https?:\\/\\/(.*@)?.+\\/(.*)\\/(.*)\\.?g?i?t?$");

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
}
