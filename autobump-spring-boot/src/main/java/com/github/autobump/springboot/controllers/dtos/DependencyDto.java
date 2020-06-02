package com.github.autobump.springboot.controllers.dtos;

import lombok.Value;

@Value
public class DependencyDto {
    String groupName;
    String artifactId;
    String versionNumber;
}
