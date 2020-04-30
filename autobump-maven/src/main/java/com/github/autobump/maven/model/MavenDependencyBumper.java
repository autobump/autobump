package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.DependencyParserException;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.Workspace;
import lombok.Getter;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.InputSource;
import org.apache.maven.model.io.xpp3.MavenXpp3ReaderEx;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.autobump.maven.model.MavenModelAnalyser.DEPENDENCY_FILENAME;

@Getter
public class MavenDependencyBumper implements DependencyBumper {
    private static final Pattern VERSION_PROPERTY_PATTERN = Pattern.compile(".*\\$\\{(.+)\\}.*");
    MavenXpp3ReaderEx mavenXpp3ReaderEx = new MavenXpp3ReaderEx();


    @Override
    public void bump(Workspace workspace, Bump bump) {
        try (Reader reader = workspace.getDependencyDocument(DEPENDENCY_FILENAME)) {
            InputLocation versionLocation = findVersionLine(reader, bump.getDependency(), workspace.getProjectRoot());
            updateDependency(versionLocation, bump);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void updateDependency(InputLocation versionLocation, Bump bump) throws IOException {
        Path file = Paths.get(versionLocation.getSource().getLocation());
        List<String> out = Files.readAllLines(file);
        Matcher matcher = VERSION_PROPERTY_PATTERN.matcher(out.get(versionLocation.getLineNumber() - 1));
        if (matcher.matches()){
            changeProperty(out, bump, matcher.group(1));
        }else {
            changeVersionLine(versionLocation, bump, out);
        }
        Files.write(file, out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void changeVersionLine(InputLocation versionLocation, Bump bump, List<String> out) {
        out.set(versionLocation.getLineNumber() - 1,
                out.get(versionLocation.getLineNumber() - 1)
                        .replace(bump.getDependency().getVersion(),
                                bump.getUpdatedVersion().getVersionNumber()));
    }

    private void changeProperty(List<String> out, Bump bump, String groupname) {
        for (int i = 0; i < out.size(); i++) {
            String line = out.get(i);
            if (line.contains("<" + groupname + ">")) {
                out.set(i, line.replace(bump.getDependency().getVersion(),
                        bump.getUpdatedVersion().getVersionNumber()));
            }
        }
    }

    private InputLocation findVersionLine(Reader reader, Dependency dependency, String rootdir) throws IOException {
        try {
            InputSource inputSource = new InputSource();
            inputSource.setLocation(rootdir + "/pom.xml");
            return mavenXpp3ReaderEx.read(reader, true, inputSource)
                    .getDependencies()
                    .stream()
                    .filter(dependency1 ->
                            dependency1.getGroupId().equals(dependency.getGroup())
                                    && dependency1.getArtifactId().equals(dependency.getName()))
                    .findFirst()
                    .orElseThrow()
                    .getLocation("version");
        } catch (XmlPullParserException e) {
            throw new DependencyParserException("error while parseing dependency file", e);
        }
    }
}
