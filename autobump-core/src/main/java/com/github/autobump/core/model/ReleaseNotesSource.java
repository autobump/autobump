package com.github.autobump.core.model;

public interface ReleaseNotesSource {
    ReleaseNotes getReleaseNotes(String projectUrl, String versionNumber);
}
