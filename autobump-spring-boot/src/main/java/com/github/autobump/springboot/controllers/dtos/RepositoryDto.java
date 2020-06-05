package com.github.autobump.springboot.controllers.dtos;

import lombok.Data;

import java.util.List;

@Data
public class RepositoryDto {
    String repoId;
    boolean selected;
    String name;
    boolean cronJob;
    String reviewer;
    List<DependencyDto> dependencies;
}
