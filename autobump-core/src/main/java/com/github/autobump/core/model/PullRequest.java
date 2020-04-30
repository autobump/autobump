package com.github.autobump.core.model;

import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder
public class PullRequest {
    String title;
    String branchName;
    String repoOwner;
    String projectName;
}
