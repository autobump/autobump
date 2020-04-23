package model;

import exceptions.DependencyParserException;
import exceptions.WrongUrlException;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.stream.Collectors;

public class MavenVersionRepository implements VersionRepository {
    private static final String URL_SUFFIX = "maven-metadata.xml";
    private final transient String baseUrl;

    public MavenVersionRepository(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Set<Version> getAllAvailableVersions(Dependency dependency) {
        try(BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(
                                    getRepoUri(dependency)
                                            .openConnection()
                                            .getInputStream()
                            )
                    )
        ) {
            return new MetadataXpp3Reader()
                    .read(in)
                    .getVersioning()
                    .getVersions()
                    .stream()
                        .map(Version::new)
                        .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new UncheckedIOException("wrong url", e);
        } catch (XmlPullParserException e) {
            throw new DependencyParserException("something went wrong while parseing the xml", e);
        }
    }

    private URL getRepoUri(Dependency dependency) {
        try {
            return new URL(this.baseUrl +
                    dependency.getGroup().replace(".", "/") +
                    "/" +
                    dependency.getName() +
                    "/" +
                    MavenVersionRepository.URL_SUFFIX);
        } catch (MalformedURLException e) {
            throw new WrongUrlException("wrong URI Syntax", e);
        }
    }
}
