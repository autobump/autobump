package com.github.autobump.core.model.domain;

import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;

@NonFinal
@Value
@SuperBuilder
public class Dependency {
    String group;
    String name;
    Version version;
}
