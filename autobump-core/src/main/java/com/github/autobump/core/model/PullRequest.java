package com.github.autobump.core.model;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder
public class PullRequest {
    @NonNull
    String title;
    @NonNull
    String branchName;
    @NonNull
    String repoOwner;
    @NonNull
    String projectName;
}
