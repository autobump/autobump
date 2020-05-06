package com.github.autobump.cli.model;

import com.github.autobump.bitbucket.exceptions.BranchNotFoundException;
import com.github.autobump.bitbucket.exceptions.RemoteNotFoundException;
import com.github.autobump.bitbucket.exceptions.UnauthorizedException;
import com.github.autobump.bitbucket.model.BitBucketAccount;
import com.github.autobump.bitbucket.model.BitBucketGitProvider;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.PullRequestResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BitBucketGitProviderTest {
    private static final String API_URL = "http://localhost:8089";
    private static final String TEST_BRANCH = "testBranch";
    private static final String TEST_OWNER = "testOwner";
    private static final String TEST_REPO_NAME = "testRepoName";
    private static final String TEST_TITLE = "testTitle";
    private BitBucketGitProvider bitBucketGitProvider;
    private WireMockServer wireMockServer;


    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        setupStub();
        bitBucketGitProvider = new BitBucketGitProvider(
                new BitBucketAccount("testUser", "testPassword"),
                API_URL
        );
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    private void setupStub() {
        wireMockServer.stubFor(post(
                urlEqualTo(String.format("/repositories/%s/%s/pullrequests", TEST_OWNER, TEST_REPO_NAME)))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("succes_response.json")));
        wireMockServer.stubFor(post(
                urlEqualTo(String.format("/repositories/%s/%s/pullrequests", "BADBRANCHE", TEST_REPO_NAME)))
                .willReturn(aResponse().withStatus(400)));
        wireMockServer.stubFor(post(
                urlEqualTo(String.format("/repositories/%s/%s/pullrequests", "BADAUTH", TEST_REPO_NAME)))
                .willReturn(aResponse().withStatus(401)));
        wireMockServer.stubFor(post(
                urlEqualTo(String.format("/repositories/%s/%s/pullrequests", "COFFEE", TEST_REPO_NAME)))
                .willReturn(aResponse().withStatus(418)));
    }

    @Test
    void makeSuccessfullPullRequest() {
        PullRequest pullRequest = PullRequest.builder()
                .repoOwner(TEST_OWNER)
                .repoName(TEST_REPO_NAME)
                .title(TEST_TITLE)
                .branchName(TEST_BRANCH)
                .build();
        var response = bitBucketGitProvider.makePullRequest(pullRequest);
        assertThat(response)
                .isEqualToComparingFieldByField(PullRequestResponse.builder()
                        .type("pullrequest")
                        .description("")
                        .link("https://bitbucket.org/grietvermeesch/testmavenproject/pull-requests/3")
                        .title("heyhey")
                        .id(3)
                        .state("OPEN")
                        .build());
    }

    @Test
    void makeRemoteNotFoundPullRequest() {
        PullRequest pullRequest = PullRequest.builder()
                .repoOwner("BADOWNER")
                .repoName(TEST_REPO_NAME)
                .title(TEST_TITLE)
                .branchName(TEST_BRANCH)
                .build();
        assertThatExceptionOfType(RemoteNotFoundException.class)
                .isThrownBy(() -> bitBucketGitProvider.makePullRequest(pullRequest));
    }

    @Test
    void makeBranchNotFoundPullRequest() {
        PullRequest pullRequest = PullRequest.builder()
                .repoOwner("BADBRANCHE")
                .repoName(TEST_REPO_NAME)
                .title(TEST_TITLE)
                .branchName(TEST_BRANCH)
                .build();
        assertThatExceptionOfType(BranchNotFoundException.class)
                .isThrownBy(() -> bitBucketGitProvider.makePullRequest(pullRequest));
    }

    @Test
    void makeUnauthorizedPullRequest() {
        PullRequest pullRequest = PullRequest.builder()
                .repoOwner("BADAUTH")
                .repoName(TEST_REPO_NAME)
                .title(TEST_TITLE)
                .branchName(TEST_BRANCH)
                .build();
        assertThatExceptionOfType(UnauthorizedException.class)
                .isThrownBy(() -> bitBucketGitProvider.makePullRequest(pullRequest));
    }

    @Test
    void makeOtherExceptionPullRequest() {
        PullRequest pullRequest = PullRequest.builder()
                .repoOwner("COFFEE")
                .repoName(TEST_REPO_NAME)
                .title(TEST_TITLE)
                .branchName(TEST_BRANCH)
                .build();
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> bitBucketGitProvider.makePullRequest(pullRequest));
    }
}
