package com.github.autobump.core.usecases;

import com.github.autobump.core.model.domain.Bump;
import com.github.autobump.core.model.domain.Dependency;
import com.github.autobump.core.model.domain.ReleaseNotes;
import com.github.autobump.core.model.releasenotes.ReleaseNotesSource;
import com.github.autobump.core.repositories.VersionRepository;
import lombok.Builder;

import java.util.Locale;

@Builder
public class FetchVersionReleaseNotesUseCase {
    private final Bump bump;
    private final VersionRepository versionRepository;
    private final ReleaseNotesSource releaseNotesSource;

    public String fetchVersionReleaseNotes() {
        StringBuilder comment = new StringBuilder();
        for (Dependency dependency : bump.getDependencies()) {
            String scmUrl = versionRepository
                    .getScmUrlForDependencyVersion(dependency, bump.getUpdatedVersion().getVersionNumber());
            if (scmUrl != null && scmUrl.toLowerCase(Locale.ROOT).startsWith("https://github.com/")) {
                ReleaseNotes releaseNotes
                        = releaseNotesSource.getReleaseNotes(scmUrl, bump.getUpdatedVersion().getVersionNumber());
                if (releaseNotes != null && !releaseNotes.getBody().isBlank()) {
                    comment.append(formatReleaseNotes(dependency, releaseNotes));
                }
            }
        }
        return comment.toString();
    }

    private String formatReleaseNotes(Dependency dependency, ReleaseNotes releaseNotes) {
        return new StringBuilder()
                .append("# :pencil: Autobump found release notes for ")
                .append(dependency.getGroup())
                .append(':')
                .append(dependency.getName())
                .append(' ')
                .append(releaseNotes.getTagName())
                .append("\n\n> ")
                .append(releaseNotes.getBody().replace("\n", "\n> "))
                .append("\n\nSource: ")
                .append(releaseNotes.getHtmlUrl())
                .append('\n')
                .toString();
    }
}
