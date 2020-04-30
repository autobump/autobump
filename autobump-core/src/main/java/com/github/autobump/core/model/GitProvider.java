package com.github.autobump.core.model;

public interface GitProvider {
    void MakePullRequest(Workspace workspace, String sourceBranch, String targetBrach, String Origin);
}
