package com.github.autobump.core.model;

public interface GitProvider {
    PullRequestResponse MakePullRequest(PullRequest pullRequest);
}
