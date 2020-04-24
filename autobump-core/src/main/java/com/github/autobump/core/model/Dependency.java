package com.github.autobump.core.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Dependency {
    String group;
    String name;
    String version;
}
