package com.github.autobump.core.repositories;

import com.github.autobump.core.model.domain.Dependency;
import com.github.autobump.core.model.domain.Version;

import java.util.Set;

public interface VersionRepository {
    Set<Version> getAllAvailableVersions(Dependency dependency);
    String getScmUrlForDependencyVersion(Dependency dependency, String versionNumber);
}
