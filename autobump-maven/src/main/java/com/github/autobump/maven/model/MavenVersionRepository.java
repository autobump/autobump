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
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class MavenVersionRepository implements VersionRepository {
    private static final transient int FILENOTFOUNDSTATUSCODE = 404;
    private final String baseUrl;
    private final HttpClient httpClient;

    public MavenVersionRepository(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .followRedirects(HttpClient.Redirect.NEVER)
                .version(HttpClient.Version.HTTP_2)
                .build();
    }

    @Override
    public Set<Version> getAllAvailableVersions(Dependency dependency) {
        try {
            HttpResponse<InputStream> response = getInputStreamHttpResponse(dependency);
            if (response.statusCode() == FILENOTFOUNDSTATUSCODE){
                return new HashSet<>();
            }
            else {
                return new MetadataXpp3Reader()
                        .read(response.body())
                        .getVersioning()
                        .getVersions()
                        .stream()
                        .map(MavenVersion::new)
                        .collect(Collectors.toUnmodifiableSet());
            }
        } catch (XmlPullParserException | IOException e) {
            throw new DependencyParserException("something went wrong while parsing the xml", e);
        }
    }

    private HttpResponse<InputStream> getInputStreamHttpResponse(Dependency dependency) {
        try {
            HttpRequest request = HttpRequest.newBuilder().GET().uri(getRepoUri(dependency).toURI())
                    .build();
            return httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (URISyntaxException e) {
            throw new WrongUrlException("Wrong URI Syntax", e);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
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
