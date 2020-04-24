package model;

import com.github.tomakehurst.wiremock.WireMockServer;
import exceptions.DependencyParserException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.*;

class MavenVersionRepositoryTest {
    private transient MavenVersionRepository mavenVersionRepository;
    private transient WireMockServer wireMockServer;
    @BeforeEach
    void setUp(){
        mavenVersionRepository = new MavenVersionRepository("http://localhost:8090/maven2/");
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
        var versions = mavenVersionRepository.getAllAvailableVersions(new Dependency("test", "test", "test"));
        assertEquals(18, versions.size());
        assertTrue(versions.contains(new Version("5.7.0-M1")));
    }

    @Test
    void getWrongXml() {
        assertThrows(DependencyParserException.class, () ->
                mavenVersionRepository.getAllAvailableVersions(new Dependency("test", "test1", "test")));
    }
}
