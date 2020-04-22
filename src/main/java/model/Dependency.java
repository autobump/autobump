package model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Dependency {
    String group;
    String name;
    String version;


    public Dependency(String group, String name, String version) {
        this.group = group;
        this.name = name;
        this.version = version;
    }
}
