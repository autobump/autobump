package com.github.autobump.springboot.controllers.dtos;

import lombok.Data;

import java.util.List;

@Data
public class RepositorySettingsDto {
    String repositoryName;
    boolean cronJob;
    String defaultReviewer;
    List<IgnoredDependencyDto> ignoredDependencyDtoList;
}
