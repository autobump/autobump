package com.github.autobump.core.model.domain;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class PullRequest {
    int pullRequestId;
    @NonNull
    final String title;
    @NonNull
    final String branchName;
    @NonNull
    final String repoOwner;
    @NonNull
    final String repoName;
    final String reviewer;
}
