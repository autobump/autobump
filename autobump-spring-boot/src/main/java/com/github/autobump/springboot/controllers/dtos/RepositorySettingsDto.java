package com.github.autobump.springboot.controllers.dtos;

import java.util.List;

public class RepositorySettingsDto {
    String repositoryName;
    boolean cronJob;
    String defaultReviewer;
    List<IgnoredDependencyDto> ignoredDependencyDtoList;
}
