package model;

import lombok.Data;

import java.util.Objects;

@Data
public class Dependency {
    private String group;
    private String name;
    private String version;


    public Dependency(String group, String name, String version) {
        this.group = group;
        this.name = name;
        this.version = version;
    }
}
