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
        StringBuilder releaseNotes = new StringBuilder();
        for (Dependency dependency : bump.getDependencies()) {
            String scmUrl = versionRepository
                    .getScmUrlForDependencyVersion(dependency, bump.getUpdatedVersion().getVersionNumber());
            if (scmUrl != null && scmUrl.toLowerCase(Locale.ROOT).startsWith("https://github.com/")) {
                String releaseNotesBody
                        = releaseNotesSource.getReleaseNotes(scmUrl, bump.getUpdatedVersion().getVersionNumber());
                if (!releaseNotesBody.isBlank()) {
                    appendVersionReleaseNotes(releaseNotes, dependency, releaseNotesBody);
                }
            }
        }
        return releaseNotes.toString();
    }

    private void appendVersionReleaseNotes(StringBuilder releaseNotes, Dependency dependency, String releaseNotesBody) {
        releaseNotes.append("# :pencil: Autobump found release notes for ")
                .append(dependency.getGroup())
                .append(':')
                .append(dependency.getName())
                .append(' ')
                .append(bump.getUpdatedVersion().getVersionNumber())
                .append("\n\n> ")
                .append(releaseNotesBody.replace("\n", "\n> "))
                .append('\n');
    }
}
