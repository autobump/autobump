package com.github.autobump.core.model.domain;

import lombok.Value;

@Value
public class ReleaseNotes {
    String htmlUrl;
    String tagName;
    String body;
}
