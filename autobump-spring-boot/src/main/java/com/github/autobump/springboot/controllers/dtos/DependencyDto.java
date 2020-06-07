package com.github.autobump.springboot.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DependencyDto {
    String groupName;
    String artifactId;
    String versionNumber;
    boolean ignoreMajor;
    boolean ignoreMinor;
}
