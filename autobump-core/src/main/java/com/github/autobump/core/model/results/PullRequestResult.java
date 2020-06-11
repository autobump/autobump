package com.github.autobump.core.model.results;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class PullRequestResult {
    String type;
    String description;
    String link;
    String title;
    int id;
    String state;
    int commentCount;
}
