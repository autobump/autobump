package com.github.autobump.springboot.controllers.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class RepositoryDto {
    int repoId;
    boolean selected;
    String name;
    List<DependencyDto> dependencies;
    boolean cronJob;
    String reviewer;
}
