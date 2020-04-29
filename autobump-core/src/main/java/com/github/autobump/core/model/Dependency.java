package com.github.autobump.core.model;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class Dependency {
    final String group;
    final String name;
    final String version;
}
