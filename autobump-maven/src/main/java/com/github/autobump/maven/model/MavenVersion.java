package com.github.autobump.maven.model;

import com.github.autobump.core.model.Version;
import lombok.Value;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.codehaus.mojo.buildhelper.versioning.VersionInformation;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

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
        var v1 = new VersionInformation(getVersionNumber());
        var v2 = new VersionInformation(otherVersion.getVersionNumber());

        Pattern pattern = Pattern.compile("^(\\d+)(?:\\.(\\d+)(?:\\.(\\d+))?)?(?:[-.]([A-Z]+))?(?![\\d.])$");
        String[] matches1 = pattern
                .matcher(getVersionNumber())
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);
        String[] matches2 = pattern
                .matcher(otherVersion.getVersionNumber())
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);
//        String[] currentVersionNodes = getVersionNumber().split("[-.]");
//        String[] otherVersionNodes = otherVersion.getVersionNumber().split("[-.]");
        return calculateUpdateType(matches1, matches2);
    }

    @SuppressWarnings("PMD.UseVarargs")
    private UpdateType calculateUpdateType(String[] currentVersionNodes, String[] otherVersionNodes) {
        UpdateType updateType;
        int size = Math.min(currentVersionNodes.length, otherVersionNodes.length);
        if (isIncrementedVersionNode(currentVersionNodes[0], otherVersionNodes[0])) {
            updateType = UpdateType.MAJOR;
        } else if (size > 1 && isIncrementedVersionNode(currentVersionNodes[1], otherVersionNodes[1])) {
            updateType = UpdateType.MINOR;
        } else {
            UpdateType b = UpdateType.NONE;
            for (int i = 2; i < size; i++) {
                if (isIncrementedVersionNode(currentVersionNodes[i], otherVersionNodes[i])) {
                    b = UpdateType.INCREMENTAL;
                    break;
                }
            }
            updateType = b;
        }
        return updateType;
    }

    private boolean isIncrementedVersionNode(String currentVersionNode, String otherVersionNode) {
        try {
            int currentNumber = Integer.parseInt(currentVersionNode);
            int otherNumber = Integer.parseInt(otherVersionNode);
            return currentNumber < otherNumber;
        } catch (NumberFormatException ex) {
            return currentVersionNode.compareTo(otherVersionNode) < 0;
        }
    }
}
