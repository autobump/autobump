package com.github.autobump.springboot.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DependencyDto {
    String groupName;
    String artifactId;
    String versionNumber;
    boolean ignoreMajor;
    boolean ignoreMinor;

    public String toString(){
        return groupName + "/" + artifactId + "/" +versionNumber;
    }

}
