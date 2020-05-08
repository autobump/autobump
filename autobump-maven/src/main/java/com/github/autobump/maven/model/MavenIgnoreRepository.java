package com.github.autobump.maven.model;

import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.IgnoreRepository;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.Version.UpdateType;

import java.util.Map;

public class MavenIgnoreRepository implements IgnoreRepository {

    private final Map<String, String> ignoreDependencies;

    public MavenIgnoreRepository(Map<String, String> ignoreDependencies) {
        this.ignoreDependencies = ignoreDependencies;
    }

    @Override
    public boolean isIgnored(Dependency dependency, Version latestVersion) {
        UpdateType updateType = dependency.getVersion().getUpdateType(latestVersion);
        boolean isIgnored = false;
        if (ignoreDependencies.containsKey(dependency.getName())) {
            isIgnored = determineIgnored(dependency.getName(), updateType);
        } else if (ignoreDependencies.containsKey(dependency.getGroup() + ":" + dependency.getName())) {
            isIgnored = determineIgnored(dependency.getGroup() + ":" + dependency.getName(), updateType);
        } else if (ignoreDependencies.containsKey(dependency.getGroup())) {
            isIgnored = determineIgnored(dependency.getGroup(), updateType);
        }
        return isIgnored;
    }

    private boolean determineIgnored(String key, UpdateType updateType) {
        boolean isIgnored = false;
        if (ignoreDependencies.get(key).equals("major") && updateType.equals(UpdateType.MAJOR)) {
            isIgnored = true;
        } else if (ignoreDependencies.get(key).equals("minor") && updateType.equals(UpdateType.MINOR)) {
            isIgnored = true;
        } else if (ignoreDependencies.get(key).equals("incremental") && updateType.equals(UpdateType.INCREMENTAL)) {
            isIgnored = true;
        }
        return isIgnored;
    }
}
