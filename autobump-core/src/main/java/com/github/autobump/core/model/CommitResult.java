package com.github.autobump.core.model;

import lombok.Value;

@Value
public class CommitResult {
    String branchName;
    String commitMessage;
}