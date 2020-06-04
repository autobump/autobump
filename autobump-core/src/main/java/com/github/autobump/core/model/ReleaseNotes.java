package com.github.autobump.core.model;

import lombok.Value;

@Value
public class ReleaseNotes {
    String htmlUrl;
    String tagName;
    String body;
}
