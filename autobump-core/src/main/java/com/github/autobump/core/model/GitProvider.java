package com.github.autobump.core.model;

import java.util.Set;

public interface GitProvider {
    PullRequestResponse makePullRequest(PullRequest pullRequest);
    Set<PullRequest> getOpenPullRequests(String repoOwner, String repoName);
    void closePullRequest(PullRequest pullRequest);
    void commentPullRequest(PullRequest pr, String comment);
}
