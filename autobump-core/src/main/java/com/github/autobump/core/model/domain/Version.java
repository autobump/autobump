package com.github.autobump.core.model.domain;

public interface Version extends Comparable<Version> {

    String getVersionNumber();

    UpdateType getUpdateType(Version otherVersion);

    enum UpdateType {
        NONE, MAJOR, MINOR, INCREMENTAL
    }
}
