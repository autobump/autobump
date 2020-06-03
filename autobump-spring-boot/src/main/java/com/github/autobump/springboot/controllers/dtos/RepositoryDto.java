package com.github.autobump.springboot.controllers.dtos;

import lombok.Data;

import java.util.List;

@Data
public class RepositoryDto {
    private boolean selected;
    String name;
    List<DependencyDto> dependencies;
    boolean cronJob;
    String reviewer;
    List<BranchDto> branches;
    int id;
    boolean ignore;
}
