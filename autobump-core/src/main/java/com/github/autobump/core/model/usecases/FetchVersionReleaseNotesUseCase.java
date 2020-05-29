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
                appendVersionReleaseNotes(versionReleasenotes, dependency, scmUrl);
            }
        }
        return versionReleasenotes.toString();
    }

    private void appendVersionReleaseNotes(StringBuilder versionReleasenotes, Dependency dependency, String scmUrl) {
        versionReleasenotes.append("# :pencil: Autobump found release notes for ")
                .append(dependency.getGroup())
                .append(':')
                .append(dependency.getName())
                .append(' ')
                .append(bump.getUpdatedVersion().getVersionNumber())
                .append("\n\n> ")
                .append(releaseNotesSource.getReleaseNotes(scmUrl, bump.getUpdatedVersion().getVersionNumber())
                        .replace("\n", "\n> "))
                .append('\n');
    }
}
