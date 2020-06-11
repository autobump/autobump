package com.github.autobump.core.events;

import lombok.Value;

import java.net.URI;

@Value
public class PushEvent {
    URI gitUri;
}
