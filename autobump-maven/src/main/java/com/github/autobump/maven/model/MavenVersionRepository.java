package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.DependencyParserException;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.VersionRepository;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class MavenVersionRepository implements VersionRepository {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    @NonNull
    private final String baseUrl;
    @NonNull
    private final MavenModelAnalyser mavenModelAnalyser;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();


    public MavenVersionRepository() {
        this("https://repo1.maven.org/maven2");
    }

    public MavenVersionRepository(String baseUrl) {
        this.baseUrl = baseUrl;
        this.mavenModelAnalyser = new MavenModelAnalyser();
    }

//    public MavenVersionRepository(String baseUrl, MavenModelAnalyser mavenModelAnalyser) {
//        this.baseUrl = baseUrl;
//        this.mavenModelAnalyser = mavenModelAnalyser;
//    }

    @Override
    public Set<Version> getAllAvailableVersions(Dependency dependency) {
        try (InputStream in = readMavenMetaDataForDependency(dependency)) {
            return new MetadataXpp3Reader()
                    .read(in)
                    .getVersioning()
                    .getVersions()
                    .stream()
                    .map(MavenVersion::new)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (XmlPullParserException e) {
            throw new DependencyParserException("Something went wrong while parsing the xml", e);
        } catch (IOException e) {
            log.warn("Unable to read maven-metadata.xml for dependency {}", dependency, e);
        }

        return Collections.emptySet();
    }

    @Override
    public String getScmUrlForDependencyVersion(Dependency dependency, String versionNumber) {
        String pomFileUrl = String.format("%s/%s/%s/%s/%s-%s.pom",
                baseUrl,
                dependency.getGroup().replaceAll("\\.", "/"),
                dependency.getName(),
                versionNumber,
                dependency.getName(),
                versionNumber
        );
        return mavenModelAnalyser.getScmUrlFromPomFile(pomFileUrl);
    }

    private InputStream readMavenMetaDataForDependency(Dependency dependency) {
        HttpRequest request = HttpRequest.newBuilder().GET()
                .uri(getMavenMetaDataUriForDependency(dependency))
                .timeout(REQUEST_TIMEOUT)
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(response -> response.statusCode() == HttpURLConnection.HTTP_OK ? response.body() : null)
                .join();
    }

    private URI getMavenMetaDataUriForDependency(Dependency dependency) {
        return URI.create(String.format("%s/%s/%s/maven-metadata.xml",
                baseUrl,
                dependency.getGroup().replace('.', '/'),
                dependency.getName()));
    }
}
