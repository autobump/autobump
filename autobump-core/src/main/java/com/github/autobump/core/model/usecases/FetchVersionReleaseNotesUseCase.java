package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.ReleaseNotesSource;
import com.github.autobump.core.model.VersionRepository;
import lombok.Builder;

import java.util.Locale;

@Builder
public class FetchVersionReleaseNotesUseCase {
    private final Bump bump;
    private final VersionRepository versionRepository;
    private final ReleaseNotesSource releaseNotesSource;

    public String fetchVersionReleaseNotes() {
        StringBuilder versionReleasenotes = new StringBuilder();
        for (Dependency dependency : bump.getDependencies()) {
            String scmUrl = versionRepository
                    .getScmUrlForDependencyVersion(dependency, bump.getUpdatedVersion().getVersionNumber());
            if (scmUrl.toLowerCase(Locale.ROOT).startsWith("https://github.com/")) {
                versionReleasenotes
                        .append(releaseNotesSource.getReleaseNotes(scmUrl, bump.getUpdatedVersion().getVersionNumber()))
                        .append('\n');
            }
        }
        return versionReleasenotes.toString();
    }
}