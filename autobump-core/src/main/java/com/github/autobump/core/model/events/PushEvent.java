package com.github.autobump.core.model.events;

import lombok.Value;

import java.net.URI;

@Value
public class PushEvent {
    String userName;
    String repoName;
    String repoOwner;
    URI uri;
}
