package model;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

class MavenVersionRepositoryTest {
    private transient MavenVersionRepository mavenVersionRepository;
    private transient WireMockServer wireMockServer;
    @BeforeEach
    void setUp(){
        mavenVersionRepository = new MavenVersionRepository();
        wireMockServer = new WireMockServer(8090);
        wireMockServer.start();
        setupStub();
    }

    private void setupStub() {
        wireMockServer.stubFor(get(urlEqualTo("https://repo1.maven.org/maven2/test/test/maven-metadata.xml"))
                .willReturn(aResponse().withHeader("Content-Type", "text/plain")
                        .withStatus(200)
                        .withBodyFile("metadata/maven-metadata.xml")));
    }

    @AfterEach
    void teardown() {
        wireMockServer.stop();
    }

    @Test
    void getAllAvailableVersions() {
        mavenVersionRepository.getAllAvailableVersions(new Dependency("test", "test", "test"));
    }
}
