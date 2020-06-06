package com.github.autobump.github.model;

import com.github.autobump.core.model.usecases.ReleaseNotes;
import com.github.autobump.github.exceptions.GithubApiException;
import com.github.autobump.github.exceptions.GithubBadRequestException;
import com.github.autobump.github.exceptions.GithubUnauthorizedException;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class GithubReleaseNotesSourceTest {

    private static final String TEST = "TEST";
    private static final String TEST_URL = "https://github.com/test/test";
    private static final String TEST_API_EXCEPTION_URL = "/repos/test/test/releases";
    private static final String TEST_GITHUB_APIURL = "http://localhost:8089";
    private static final String TEST_GETTAGS_MOCKURL = "/repos/spring-projects/spring-boot/releases";
    private static final String TEST_PROJECTURL = "https://github.com/spring-projects/spring-boot";
    private static final String TEST_NOTAGS_PROJECTURL = "https://github.com/test/notags";
    private static final String TEST_GETTAGS_NONE_MOCKURL = "/repos/test/notags/releases";
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
        githubReleaseNotesSource = new GithubReleaseNotesSource(TEST_GITHUB_APIURL);
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void getReleaseNotes_returnsReleaseNotesNotNull() {
        setupDefaultStubs();
        ReleaseNotes result = githubReleaseNotesSource.getReleaseNotes(TEST_PROJECTURL, TEST_VERSIONNUMBER);
        assertThat(result).isNotNull();
    }

    @Test
    void getReleaseNotes_returnsReleaseNotesisNull() {
        setupDefaultStubs();
        ReleaseNotes result = githubReleaseNotesSource.getReleaseNotes(TEST_NOTAGS_PROJECTURL, TEST_VERSIONNUMBER);
        assertThat(result).isNull();
    }

    @Test
    void getReleaseNotes_returnsCorrectReleaseNotes() {
        setupDefaultStubs();
        ReleaseNotes result = githubReleaseNotesSource.getReleaseNotes(TEST_PROJECTURL, TEST_VERSIONNUMBER);
        assertThat(result.getBody()).isEqualTo(TEST_RELEASENOTESSAMPLE);
        wireMockServer.verify(1, getRequestedFor(urlEqualTo(TEST_GETTAGS_MOCKURL)));
    }

    @Test
    void getReleaseNotes_ThrowsGithubBadRequestException() {
        setupErrorStub(400);
        assertThatExceptionOfType(GithubBadRequestException.class)
                .isThrownBy(() -> githubReleaseNotesSource.getReleaseNotes(TEST_URL, TEST));
    }

    @Test
    void getReleaseNotes_ThrowsGithubUnauthorizedException() {
        setupErrorStub(401);
        assertThatExceptionOfType(GithubUnauthorizedException.class)
                .isThrownBy(() -> githubReleaseNotesSource.getReleaseNotes(TEST_URL, TEST));
    }

    /*@Test
    void getReleaseNotes_ThrowsGithubNotFoundException() {
        setupErrorStub(404);
        assertThatExceptionOfType(GithubNotFoundException.class)
                .isThrownBy(() -> githubReleaseNotesSource.getReleaseNotes(TEST_URL, TEST));
    }*/

    @Test
    void getReleaseNotes_ThrowsGithubApiException() {
        setupErrorStub(500);
        assertThatExceptionOfType(GithubApiException.class)
                .isThrownBy(() -> githubReleaseNotesSource.getReleaseNotes(TEST_URL, TEST));
    }

    private void setupDefaultStubs() {
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

    private void setupErrorStub(int statusCode){
        wireMockServer.stubFor(get(
                urlEqualTo(TEST_API_EXCEPTION_URL))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(statusCode)));
    }
}
