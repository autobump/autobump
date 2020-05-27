package com.github.autobump.core.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class UseCaseConfiguration {
    @NonNull
    GitProvider gitProvider;
    @NonNull
    GitClient gitClient;
    @NonNull
    DependencyResolver dependencyResolver;
    @NonNull
    VersionRepository versionRepository;
    @NonNull
    DependencyBumper dependencyBumper;
    @NonNull
    UrlHelper urlHelper;
    @NonNull
    IgnoreRepository ignoreRepository;
}
