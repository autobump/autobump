package com.github.autobump.maven.model;

import com.github.autobump.core.model.Version;
import lombok.Value;
import org.apache.maven.artifact.versioning.ComparableVersion;

@Value
public class MavenVersion implements Version {
    String versionNumber;


    @Override
    public int compareTo(Version o) {
        ComparableVersion comparableVersion = new ComparableVersion(versionNumber);
        return new ComparableVersion(o.getVersionNumber()).compareTo(comparableVersion);
    }

    @Override
    public UpdateType getUpdateType(Version otherVersion) {
        var explodedCurrent = getVersionNumber().split("\\.");
        var explodedOther = otherVersion.getVersionNumber().split("\\.");
        UpdateType updateType = UpdateType.NONE;
        int size = Math.min(explodedCurrent.length, explodedOther.length);

        if (explodedCurrent[0].compareTo(explodedOther[0]) < 0) {
            updateType = UpdateType.MAJOR;
        } else if (size > 1 && explodedCurrent[1].compareTo(explodedOther[1]) < 0) {
            updateType = UpdateType.MINOR;
        }

        for (int i = 2; i < size; i++) {
            if (explodedCurrent[i].compareTo(explodedOther[i]) < 0) {
                updateType = UpdateType.INCREMENTAL;
                break;
            }
        }

        return updateType;
    }
}
