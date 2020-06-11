package com.github.autobump.core.model.results;

import lombok.Value;

@Value
public class CommitResult {
    String branchName;
    String commitMessage;
}
