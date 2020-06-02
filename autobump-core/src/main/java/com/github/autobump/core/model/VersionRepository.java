package com.github.autobump.core.model;

import java.util.Set;

public interface VersionRepository {
    Set<Version> getAllAvailableVersions(Dependency dependency);
    String getScmUrlForDependencyVersion(Dependency dependency, String versionNumber);
}
