package com.github.autobump.github.model;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GithubReleaseNotesSourceTest {

    private GithubReleaseNotesSource githubReleaseNotesSource;
    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        githubReleaseNotesSource = new GithubReleaseNotesSource();
    }

    @Test
    void getReleaseNotes_returnsReleaseNotes(){
        String result = githubReleaseNotesSource.getReleaseNotes("https://github.com/spring-projects/spring-boot","2.3.0.RELEASE");
        assertThat(result).isNotNull();
    }
}
