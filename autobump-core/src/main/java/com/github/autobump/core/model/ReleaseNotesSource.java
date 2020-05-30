package com.github.autobump.core.model;

import com.github.autobump.core.model.usecases.ReleaseNotes;

public interface ReleaseNotesSource {
    ReleaseNotes getReleaseNotes(String projectUrl, String versionNumber);
}
