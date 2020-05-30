package com.github.autobump.github.model;

import com.github.autobump.core.model.usecases.ReleaseNotes;
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
    private static final String TEST_GETTAGS_MOCKURL = "/repos/spring-projects/spring-boot/releases";
//    private static final String TEST_TAG_MOCKURL = "/repos/spring-projects/spring-boot/releases/tags/v2.3.0.RELEASE";
    private static final String TEST_PROJECTURL = "https://github.com/spring-projects/spring-boot";
    private static final String TEST_NOTAGS_PROJECTURL = "https://github.com/test/notags";
    private static final String TEST_GETTAGS_NONE_MOCKURL = "/repos/test/notags/releases";
//    private static final String TEST_RELEASENOTESSAMPLE_JSON
//            = "{ \"tag_name\" : \"v2.3.0.RELEASE\",\"body\" : \"RELEASE NOTES\\nRelease notes sample text\" }";
    private static final String TEST_ALLRELEASENOTESTAGS_JSON
            = "[{\"tag_name\":\"v2.2.0.RELEASE\",\"body\":\"RELEASE NOTES\\nRelease notes sample text\"}," +
            "{\"tag_name\":\"v2.3.0.RELEASE\",\"body\":\"RELEASE NOTES\\nRelease notes sample text\"}]";
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
        ReleaseNotes result = githubReleaseNotesSource.getReleaseNotes(TEST_PROJECTURL, TEST_VERSIONNUMBER);
        assertThat(result).isNotNull();
    }

    @Test
    void getReleaseNotes_returnsReleaseNotesisNull() {
        ReleaseNotes result = githubReleaseNotesSource.getReleaseNotes(TEST_NOTAGS_PROJECTURL, TEST_VERSIONNUMBER);
        assertThat(result).isNull();
    }

    @Test
    void getReleaseNotes_returnsCorrectReleaseNotes() {
        ReleaseNotes result = githubReleaseNotesSource.getReleaseNotes(TEST_PROJECTURL, TEST_VERSIONNUMBER);
        assertThat(result.getBody()).isEqualTo(TEST_RELEASENOTESSAMPLE);
        wireMockServer.verify(1, getRequestedFor(urlEqualTo(TEST_GETTAGS_MOCKURL)));
    }

    private void setupStubs() {
        wireMockServer.stubFor(get(
                urlEqualTo(TEST_GETTAGS_NONE_MOCKURL))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("[]")));

        wireMockServer.stubFor(get(
                urlEqualTo(TEST_GETTAGS_MOCKURL))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(TEST_ALLRELEASENOTESTAGS_JSON)));
    }
}
