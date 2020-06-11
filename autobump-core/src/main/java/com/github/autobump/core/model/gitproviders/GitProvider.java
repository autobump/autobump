package com.github.autobump.core.model.gitproviders;

import com.github.autobump.core.model.domain.PullRequest;
import com.github.autobump.core.model.domain.Repo;
import com.github.autobump.core.model.results.PullRequestResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GitProvider {
    PullRequestResult makePullRequest(PullRequest pullRequest);
    Set<PullRequest> getOpenPullRequests(String repoOwner, String repoName);
    void closePullRequest(PullRequest pullRequest);
    void commentPullRequest(PullRequest pr, String comment);
    List<Repo> getRepos();
    Map<String, String> getMembersFromWorkspace(Repo repo);
    String getCurrentUserUuid();
}
