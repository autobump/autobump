package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.IgnoreRepository;
import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.VersionRepository;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class BumpResolverUseCase {
    private Set<Dependency> dependencies;
    private IgnoreRepository ignoreRepository;
    private VersionRepository versionRepository;
    private SettingsRepository settingsRepository;

    public Set<Bump> doResolve(String repoName) {
        return groupBumps(makeBumpSet(repoName));
    }

    public Set<Bump> makeBumpSet(String repoName) {
        Set<Bump> bumps = new HashSet<>();
        for (Dependency dependency : dependencies) {
            Version latestVersion = getUpdateVersion(dependency);
            if (latestVersion != null
                    && dependency.getVersion().compareTo(latestVersion) > 0
                    && !isIgnored(dependency, latestVersion, repoName)) {
                //newer version is found && bump is not ignored => make bump
                bumps.add(new Bump(Set.of(dependency), latestVersion));
            }
        }
        return bumps;
    }

    @SuppressWarnings("checkstyle:ReturnCount")
    private boolean isIgnored(Dependency dependency, Version latestVersion, String repoName) {
        var settings = settingsRepository.getAllIgnores().stream()
                .filter(setting -> setting.getRepositoryName().equalsIgnoreCase(repoName))
                .collect(Collectors.toList());
        for (Setting setting : settings) {
            String[] dep = setting.getKey().split(":");
            if (dep.length == 2) {
                if (dep[0].equalsIgnoreCase(dependency.getGroup()) && dep[1].equalsIgnoreCase(dependency.getName())) {
                    return latestVersion.getVersionNumber().equalsIgnoreCase(setting.getValue());
                }
            } else {
                return dep[0].equalsIgnoreCase(dependency.getGroup())
                        && dep[1].equalsIgnoreCase(dependency.getName())
                        && dep[2].equalsIgnoreCase(dependency.getVersion().getVersionNumber())
                        && dep[3].equalsIgnoreCase(latestVersion.getVersionNumber());
            }
        }
        return false;
    }

    private Set<Bump> groupBumps(Set<Bump> bumps) {
        return bumps.stream().collect(Collectors.collectingAndThen(
                Collectors.toUnmodifiableMap(b -> b.getGroup() + ":" + b.getUpdatedVersion(), b -> b, Bump::combine),
                map -> Set.copyOf(map.values())));
    }

    private Version getUpdateVersion(Dependency dependency) {
        Version latestVersion = getLatestVersion(dependency);
        if (latestVersion != null && ignoreRepository.isIgnored(dependency, latestVersion)) {
            latestVersion = null;
        }
        return latestVersion;
    }

    private Version getLatestVersion(Dependency dependency) {
        return versionRepository.getAllAvailableVersions(dependency).stream()
                .sorted().findFirst().orElse(null);
    }
}
