package com.github.autobump.springboot.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DependencyDto {
    int id;
    String groupName;
    String artifactId;
    String versionNumber;
    boolean ignoreMajor;
    boolean ignoreMinor;

    public DependencyDto() {
    }

    public String toString(){
        return groupName + "/" + artifactId + "/" +versionNumber;
    }

}
