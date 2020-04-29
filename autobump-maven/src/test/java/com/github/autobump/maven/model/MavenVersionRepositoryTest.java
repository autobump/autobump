package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.DependencyParserException;
import com.github.autobump.core.model.Dependency;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MavenVersionRepositoryTest {
    private static final transient String TEST = "test";
    private transient MavenVersionRepository mavenVersionRepository;
    private transient WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        mavenVersionRepository = new MavenVersionRepository("http://localhost:8090/maven2");
        wireMockServer = new WireMockServer(options().port(8090).usingFilesUnderClasspath("src/test/resources/"));
        wireMockServer.start();
        setupStub();
    }

    private void setupStub() {
        wireMockServer.stubFor(get(urlEqualTo("/maven2/test/test/maven-metadata.xml"))
                .willReturn(aResponse().withHeader("Content-Type", "text/xml")
                        .withStatus(200)
                        .withBodyFile("metadata/maven-metadata.xml")));
        wireMockServer.stubFor(get(urlEqualTo("/maven2/test/test1/maven-metadata.xml"))
                .willReturn(aResponse().withHeader("Content-Type", "text/xml")
                        .withStatus(200)
                        .withBodyFile("metadata/maven-metadata-corrupt.xml")));
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void getAllAvailableVersions() {
        var versions = mavenVersionRepository.getAllAvailableVersions(
                Dependency.builder().name(TEST).group(TEST).version(TEST).build());
        assertEquals(18, versions.size());
        assertTrue(versions.contains(new MavenVersion("5.7.0-M1")));
    }

    @Test
    void getWrongXml() {
        assertThrows(DependencyParserException.class, () ->
                mavenVersionRepository.getAllAvailableVersions(
                        Dependency.builder().name("test1").group(TEST).version(TEST).build()));
    }

    @Test
    void getFileNotFound() {
        assertEquals(new HashSet<>(),
                mavenVersionRepository.getAllAvailableVersions(
                        Dependency.builder().name("bla").group("bla").version("bla").build()));
    }

    @Test
    void testMalformedUrl() {
        assertThrows(IllegalArgumentException.class, () ->
                new MavenVersionRepository("//").getAllAvailableVersions(
                        Dependency.builder().name(TEST).group(TEST).version(TEST).build()));
    }
}
