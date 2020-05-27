package com.github.autobump.core.model;

public interface ReleaseNotesSource {
    String getReleaseNotes(String projectUrl, String versionNumber);
}
