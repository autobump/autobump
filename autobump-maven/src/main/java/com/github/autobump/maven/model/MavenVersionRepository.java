package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.DependencyParserException;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.maven.exceptions.WrongUrlException;
import lombok.Getter;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Getter
public class MavenVersionRepository implements VersionRepository {
    private static final int STATUSCODE_OK = 200;
    private static final int TIMEOUT = 30;
    private final String baseUrl;

    public MavenVersionRepository(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public Set<Version> getAllAvailableVersions(Dependency dependency) {
        InputStream in = getInputStream(dependency);
        try {
            return new MetadataXpp3Reader()
                    .read(in)
                    .getVersioning()
                    .getVersions()
                    .stream()
                    .map(MavenVersion::new)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (XmlPullParserException e) {
            throw new DependencyParserException("something went wrong while parseing the xml", e);
        } catch (IOException e) {
            return new HashSet<>();
        }
    }

    private InputStream getInputStream(Dependency dependency) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().GET().uri(getRepoUri(dependency))
                .timeout(Duration.ofSeconds(TIMEOUT)).build();

        CompletableFuture<HttpResponse<InputStream>> future = httpClient.sendAsync(request, responseInfo
                -> responseInfo.statusCode() == STATUSCODE_OK ?
                HttpResponse.BodySubscribers.ofInputStream() :
                HttpResponse.BodySubscribers.replacing(null)
        );
        return future.thenApply(HttpResponse::body).join();
    }

    private URI getRepoUri(Dependency dependency) {
        try {
            return new URL(String.format("%s/%s/%s/maven-metadata.xml",
                    baseUrl,
                    dependency.getGroup().replaceAll("\\.", "/"),
                    dependency.getName())).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new WrongUrlException("wrong URI Syntax", e);
        }
    }
}
