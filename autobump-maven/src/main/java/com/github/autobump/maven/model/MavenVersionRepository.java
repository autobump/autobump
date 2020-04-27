package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.DependencyParserException;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.maven.exceptions.WrongUrlException;
import lombok.Getter;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class MavenVersionRepository implements VersionRepository {
    private final String baseUrl;

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
            return (Set<Version>) new MetadataXpp3Reader()
                    .read(in)
                    .getVersioning()
                    .getVersions()
                    .stream()
                        .map(v -> new MavenVersion(v.toString()))
                        .collect(Collectors.toSet());
        } catch (XmlPullParserException e) {
            throw new DependencyParserException("something went wrong while parseing the xml", e);
        }catch (IOException e) {
            return new HashSet<>();
        }
    }

    private URL getRepoUri(Dependency dependency) {
        try {
            return new URL(String.format("%s/%s/%s/maven-metadata.xml",
                    baseUrl,
                    dependency.getGroup().replaceAll("\\.", "/"),
                    dependency.getName()));
        } catch (MalformedURLException e) {
            throw new WrongUrlException("wrong URI Syntax", e);
        }
    }
}
