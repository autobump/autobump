package com.github.autobump.maven.model;

import com.github.autobump.core.exceptions.DependencyParserException;
import com.github.autobump.core.model.Dependency;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MavenVersionRepositoryTest {
    private static final String TEST_MAVEN_URL = "http://localhost:8090/maven2";
    private static final String TEST_CORRECT_PROJ_URL = "http://correct.project.url";
    private static final transient String TEST = "test";
    private transient MavenVersionRepository mavenVersionRepository;
    private transient WireMockServer wireMockServer;
    private Dependency dependency;

    @Mock
    private MavenModelAnalyser mavenModelAnalyser;

    @BeforeEach
    void setUp() {
        mavenVersionRepository = new MavenVersionRepository(TEST_MAVEN_URL, mavenModelAnalyser);
        wireMockServer = new WireMockServer(options().port(8090).usingFilesUnderClasspath("src/test/resources/"));
        wireMockServer.start();
        initDependency();
        setupStub();
    }


    @Test
    void getScmUrlForDependencyVersion_returnsCorrecturl() {
        setupMock();
        String result = mavenVersionRepository.getScmUrlForDependencyVersion(dependency, TEST);
        assertThat(result).isEqualTo(TEST_CORRECT_PROJ_URL);
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
                dependency);
        assertThat(versions).hasSize(18);
        assertThat(versions).contains(new MavenVersion("5.7.0-M1"));
    }

    @Test
    void getWrongXml() {
        assertThatExceptionOfType(DependencyParserException.class).isThrownBy(() ->
                mavenVersionRepository.getAllAvailableVersions(
                        Dependency.builder().name("test1").group(TEST)
                                .version(new MavenVersion(TEST)).build()));
    }

    @Test
    void getFileNotFound() {
        assertThat(mavenVersionRepository.getAllAvailableVersions(
                Dependency.builder().name("bla").group("bla")
                        .version(new MavenVersion("bla")).build())).isEmpty();
    }

    @Test
    void testMalformedUrl() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() ->
                new MavenVersionRepository("//").getAllAvailableVersions(
                        Dependency.builder().name(TEST).group(TEST)
                                .version(new MavenVersion(TEST)).build()));
    }

    private void initDependency() {
        dependency = Dependency.builder().name(TEST).group(TEST)
                .version(new MavenVersion(TEST)).build();
    }

    private void setupMock() {
        when(mavenModelAnalyser.getScmUrlFromPomFile(TEST_MAVEN_URL + "/test/test/test/test-test.pom"))
                .thenReturn(TEST_CORRECT_PROJ_URL);
    }
}
