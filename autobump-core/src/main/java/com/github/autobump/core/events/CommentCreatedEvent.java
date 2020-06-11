package com.github.autobump.core.events;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CommentCreatedEvent {
    String repositoryName;
    String pullRequestTitle;
    String comment;
}
