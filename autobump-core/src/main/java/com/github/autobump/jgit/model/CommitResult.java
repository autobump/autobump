package com.github.autobump.jgit.model;

import lombok.Value;

@Value
public class CommitResult {
    String branchName;
    String commitMessage;
}
