package com.github.autobump.maven.model;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.Workspace;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.maven.model.io.xpp3.MavenXpp3ReaderEx;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class MavenDependencyBumper implements DependencyBumper {
    private static final Pattern VERSION_PROPERTY_PATTERN = Pattern.compile(".*\\$\\{(.+)\\}.*");
    MavenXpp3ReaderEx mavenXpp3ReaderEx = new MavenXpp3ReaderEx();


    @Override
    public void bump(Workspace workspace, Bump bump) {
        try {
            for (Dependency dependency : bump.getDependencies()) {
                if (dependency instanceof MavenDependency &&
                        ((MavenDependency) dependency).getInputLocation() != null) {
                    MavenUpdate u = new MavenUpdate((MavenDependency) dependency, bump.getUpdatedVersion());
                    updateDependency(u);
                } else {
                    throw new IllegalArgumentException(dependency.toString() + " not of type MavenDependency");
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void updateDependency(MavenUpdate update)
            throws IOException {
        Path file = Paths.get(update.getDependency().getInputLocation().getSource().getLocation());
        List<String> out = Files.readAllLines(file);
        Matcher matcher = VERSION_PROPERTY_PATTERN.matcher(
                out.get(update.getDependency().getInputLocation().getLineNumber() - 1));
        if (matcher.matches()) {
            changeProperty(out, update, matcher.group(1));
        } else {
            changeVersionLine(out, update);
        }
        Files.write(file, out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void changeVersionLine(List<String> out, MavenUpdate update) {
        out.set(update.getDependency().getInputLocation().getLineNumber() - 1,
                out.get(update.getDependency().getInputLocation().getLineNumber() - 1)
                        .replace(update.getDependency().getVersion().getVersionNumber(),
                                update.getVersion().getVersionNumber()));
    }

    private void changeProperty(List<String> out, MavenUpdate update, String propertyName) {
        for (int i = 0; i < out.size(); i++) {
            String line = out.get(i);
            if (line.contains("<" + propertyName + ">")) {
                out.set(i, line.replace(update.getDependency().getVersion().getVersionNumber(),
                        update.getVersion().getVersionNumber()));
            }
        }
    }

    @Getter
    @AllArgsConstructor
    public static class MavenUpdate {
        private final MavenDependency dependency;
        private final Version version;
    }
}
