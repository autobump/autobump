package com.github.autobump.core.model;

import java.util.List;

public interface GitProvider {
    PullRequestResponse makePullRequest(PullRequest pullRequest);
    List<PullRequest> getOpenPullRequests(String repoOwner, String repoName);
}
