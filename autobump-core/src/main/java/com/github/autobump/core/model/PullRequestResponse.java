package com.github.autobump.core.model;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class PullRequestResponse {
    String type;
    String description;
    String link;
    String title;
    int id;
    String state;
    int commentCount;
}
