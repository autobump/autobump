package com.github.autobump.core.usecases;

import com.github.autobump.core.model.dependencymanagement.DependencyBumper;
import com.github.autobump.core.model.dependencymanagement.DependencyResolver;
import com.github.autobump.core.model.gitclients.GitClient;
import com.github.autobump.core.model.gitproviders.GitProvider;
import com.github.autobump.core.model.gitproviders.GitProviderUrlHelper;
import com.github.autobump.core.repositories.IgnoreRepository;
import com.github.autobump.core.repositories.VersionRepository;
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
    GitProviderUrlHelper gitProviderUrlHelper;
    @NonNull
    IgnoreRepository ignoreRepository;
}
