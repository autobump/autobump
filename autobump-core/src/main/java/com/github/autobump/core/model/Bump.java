package com.github.autobump.core.model;

import lombok.Value;

@Value
public class Bump {
    Dependency dependency;
    Version version;
}
