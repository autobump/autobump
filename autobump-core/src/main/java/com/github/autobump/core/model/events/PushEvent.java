package com.github.autobump.core.model.events;

import lombok.Value;

import java.net.URI;

@Value
public class PushEvent {
    URI uri;
}
