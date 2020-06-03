package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.IgnoreRepository;
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

    public Set<Bump> doResolve(){
        return groupBumps(makeBumpSet());
    }

    public Set<Bump> makeBumpSet() {
        Set<Bump> bumps = new HashSet<>();
        for (Dependency dependency : dependencies) {
            Version latestVersion = getUpdateVersion(dependency);
            if (latestVersion != null && dependency.getVersion().compareTo(latestVersion) > 0) {
                //newer version is found => make bump
                bumps.add(new Bump(Set.of(dependency), latestVersion));
            }
        }
        return bumps;
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