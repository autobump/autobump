package com.github.autobump.maven.model;

import com.github.autobump.core.model.Version;
import lombok.Value;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.codehaus.mojo.buildhelper.versioning.VersionInformation;

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
        if (this.compareTo(otherVersion) <= 0) {
            return UpdateType.NONE;
        }
        var thisVersionInfo = new VersionInformation(getVersionNumber());
        var otherVersionInfo = new VersionInformation(otherVersion.getVersionNumber());
        return calculateUpdateType(thisVersionInfo, otherVersionInfo);
    }

    private UpdateType calculateUpdateType(VersionInformation thisVersionInfo, VersionInformation otherVersionInfo) {
        UpdateType updateType;
        if (thisVersionInfo.getMajor() < otherVersionInfo.getMajor()) {
            updateType = UpdateType.MAJOR;
        } else if (thisVersionInfo.getMinor() < otherVersionInfo.getMinor()
                || thisVersionInfo.getQualifier() != null && otherVersionInfo.getQualifier() == null) {
            updateType = UpdateType.MINOR;
        } else {
            updateType = UpdateType.INCREMENTAL;
        }
        return updateType;
    }
}
