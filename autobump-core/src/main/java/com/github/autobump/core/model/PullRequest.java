package com.github.autobump.core.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class PullRequest {
    @NonNull
    String title;
    @NonNull
    String branchName;
    @NonNull
    String repoOwner;
    @NonNull
    String repoName;
}
