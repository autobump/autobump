package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.DependencyParserException;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.maven.exceptions.DependencyNotFoundException;
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

    @Override
    public void bump(Workspace workspace, Bump bump) {
        try(Reader reader = workspace.getDependencyDocument(MavenDependencyResolver.DEPENDENCY_FILENAME)) {
            InputLocation versionLocation = findVersionLine(reader, bump.getDependency());
            updateDependency(versionLocation, bump);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void updateDependency(InputLocation versionLocation, Bump bump) {
        try {
            Path file = Paths.get(versionLocation.getSource().getLocation());
            List<String> out = Files.readAllLines(file);
            out.set(versionLocation.getLineNumber() - 1,
                    out.get(versionLocation.getLineNumber() - 1)
                            .replace(bump.getDependency().getVersion(),
                                    bump.getVersion().getVersionNumber()));
            Files.write(file, out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private InputLocation findVersionLine(Reader reader, Dependency dependency) {
        MavenXpp3ReaderEx mavenXpp3ReaderEx = new MavenXpp3ReaderEx();
        try {
            InputSource inputSource = new InputSource();
            inputSource.setLocation("src/test/resources/project_root/pom.xml");
            return mavenXpp3ReaderEx.read(reader, true, inputSource)
                    .getDependencies()
                    .stream()
                    .filter(dependency1 ->
                            dependency1.getGroupId().equals(dependency.getGroup())
                                    && dependency1.getArtifactId().equals(dependency.getName()))
                    .findFirst()
                    .orElseThrow(() ->
                            new DependencyNotFoundException("clould not find dependency " + dependency.getName()))
                    .getLocation("version");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (XmlPullParserException e) {
            throw new DependencyParserException("error while parseing dependency file", e);
        }
    }
}
