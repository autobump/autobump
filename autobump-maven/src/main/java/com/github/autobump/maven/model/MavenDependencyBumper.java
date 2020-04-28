package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.DependencyParserException;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.Workspace;
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

public class MavenDependencyBumper implements DependencyBumper {

    transient MavenXpp3ReaderEx mavenXpp3ReaderEx = new MavenXpp3ReaderEx();

    @Override
    public void bump(Workspace workspace, Bump bump) {
        try (Reader reader = workspace.getDependencyDocument(MavenDependencyResolver.DEPENDENCY_FILENAME)) {
            InputLocation versionLocation = findVersionLine(reader, bump.getDependency(), workspace.getProjectRoot());
            updateDependency(versionLocation, bump);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void updateDependency(InputLocation versionLocation, Bump bump) throws IOException {
        Path file = Paths.get(versionLocation.getSource().getLocation());
        List<String> out = Files.readAllLines(file);
        if (out.get(versionLocation.getLineNumber() - 1).matches(".*\\$\\{.*\\}.*")){
            changeProperty(versionLocation, out, bump);
        }else {
            changeVersionLine(versionLocation, bump, out);
        }
        Files.write(file, out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void changeVersionLine(InputLocation versionLocation, Bump bump, List<String> out) {
        out.set(versionLocation.getLineNumber() - 1,
                out.get(versionLocation.getLineNumber() - 1)
                        .replace(bump.getDependency().getVersion(),
                                bump.getVersion().getVersionNumber()));
    }

    private void changeProperty(InputLocation versionLocation, List<String> out, Bump bump) {
        for (int i = 0; i < out.size(); i++) {
            String line = out.get(i);
            if (line.contains("<" +
                    out.get(versionLocation.getLineNumber() - 1)
                            .substring(
                                    out.get(versionLocation.getLineNumber() - 1).indexOf("{") + 1,
                                    out.get(versionLocation.getLineNumber() - 1).indexOf("}") - 1))) {
                out.set(i, line.replace(bump.getDependency().getVersion(), bump.getVersion().getVersionNumber()));
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
