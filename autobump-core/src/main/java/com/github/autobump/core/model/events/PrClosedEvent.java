package com.github.autobump.core.model.events;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
@Builder
public class PrClosedEvent {
    private static final Pattern BUMP_TITLE_PATTERN = Pattern.compile("Bumped (.+:.+:.+)+ to version: (.+)");
    String prName;
    String repoName;

    public Bump getBump() {
        Matcher matcher = BUMP_TITLE_PATTERN.matcher(prName);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(prName);
        }
        var dependencies = parseDependencies(matcher.group(1));
        return new Bump(dependencies, new DependencyVersion(matcher.group(2)));
    }

    private Set<Dependency> parseDependencies(String dependencies) {
        Set<Dependency> deps = new HashSet<>();
        for (String dependencystring : dependencies.split(" and ")) {
            var dependencyStrings = dependencystring.split(":");
            deps.add(
                    Dependency.builder()
                            .group(dependencyStrings[0])
                            .name(dependencyStrings[1])
                            .version(new DependencyVersion(dependencyStrings[2]))
                            .build()
            );
        }
        return deps;
    }

    @AllArgsConstructor
    static class DependencyVersion implements Version {
        private final String versionNumber;

        @Override
        public String getVersionNumber() {
            return versionNumber;
        }

        @Override
        public UpdateType getUpdateType(Version otherVersion) {
            return null;
        }

        @Override
        public int compareTo(Version o) {
            return 0;
        }
    }
}
