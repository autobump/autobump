package com.github.autobump.core.model.releasenotes;

import com.github.autobump.core.model.domain.ReleaseNotes;

public interface ReleaseNotesSource {
    ReleaseNotes getReleaseNotes(String projectUrl, String versionNumber);
}
