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
}
