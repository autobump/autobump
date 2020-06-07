package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.ReleaseNotes;
import com.github.autobump.core.model.ReleaseNotesSource;
import com.github.autobump.core.model.VersionRepository;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Locale;

@Named
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class FetchVersionReleaseNotesUseCase {
    private final VersionRepository versionRepository;
    private final ReleaseNotesSource releaseNotesSource;

    public String fetchVersionReleaseNotes(Bump bump) {
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
