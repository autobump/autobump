package com.github.autobump.github.model;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

class GithubReleaseNotesSourceTest {

    private static final String TEST_GITHUB_APIURL = "http://localhost:8089";
    private static final String TEST_MOCKURL = "/repos/spring-projects/spring-boot/releases/tags/v2.3.0.RELEASE";
    private static final String TEST_PROJECTURL = "https://github.com/spring-projects/spring-boot";
    private static final String TEST_RELEASENOTESSAMPLE_JSON
            = "{ \"body\" : \"RELEASE NOTES\\nRelease notes sample text\" }";
    private static final String TEST_RELEASENOTESSAMPLE = "RELEASE NOTES\nRelease notes sample text";
    private static final String TEST_VERSIONNUMBER = "2.3.0.RELEASE";

    private GithubReleaseNotesSource githubReleaseNotesSource;
    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        setupStubs();
        githubReleaseNotesSource = new GithubReleaseNotesSource(TEST_GITHUB_APIURL);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void getReleaseNotes_returnsReleaseNotesNotNull() {
        String result = githubReleaseNotesSource.getReleaseNotes(TEST_PROJECTURL, TEST_VERSIONNUMBER);
        assertThat(result).isNotNull();
    }

    @Test
    void getReleaseNotes_returnsCorrectReleaseNotes() {
        String result = githubReleaseNotesSource.getReleaseNotes(TEST_PROJECTURL, TEST_VERSIONNUMBER);
        assertThat(result).isEqualTo(TEST_RELEASENOTESSAMPLE);
        wireMockServer.verify(1, getRequestedFor(urlEqualTo(TEST_MOCKURL)));
    }

    private void setupStubs() {
        wireMockServer.stubFor(get(
                urlEqualTo(TEST_MOCKURL))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(TEST_RELEASENOTESSAMPLE_JSON)));
    }
}
