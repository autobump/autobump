package com.github.autobump.bitbucket.model;

import com.github.autobump.bitbucket.exceptions.BitbucketBadRequestException;
import com.github.autobump.bitbucket.exceptions.BitbucketNotFoundException;
import com.github.autobump.bitbucket.exceptions.BitbucketUnauthorizedException;
import com.github.autobump.core.model.domain.PullRequest;
import com.github.autobump.core.model.domain.Repo;
import com.github.autobump.core.model.results.PullRequestResult;
import com.github.tomakehurst.wiremock.WireMockServer;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class BitBucketGitProviderTest {
    private static final String REPO_URL = "/repositories/%s/%s/pullrequests";
    private static final String API_URL = "http://localhost:8089";
    private static final String TEST_BRANCH = "testBranch";
    private static final String TEST_OWNER = "testOwner";
    private static final String TEST_REPO_NAME = "testRepoName";
    private static final String TEST_TITLE = "testTitle";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
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
                urlEqualTo(String.format(REPO_URL, TEST_OWNER, TEST_REPO_NAME)))
                .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withStatus(200).withBodyFile("succes_response.json")));
        wireMockServer.stubFor(post(
                urlEqualTo(String.format(REPO_URL, "BADBRANCHE", TEST_REPO_NAME)))
                .willReturn(aResponse().withStatus(400)));
        wireMockServer.stubFor(post(
                urlEqualTo(String.format(REPO_URL, "BADAUTH", TEST_REPO_NAME)))
                .willReturn(aResponse().withStatus(401)));
        wireMockServer.stubFor(post(
                urlEqualTo(String.format(REPO_URL, "COFFEE", TEST_REPO_NAME)))
                .willReturn(aResponse().withStatus(418)));
        wireMockServer.stubFor(get(
                urlEqualTo(String.format(REPO_URL, TEST_OWNER, TEST_REPO_NAME)))
                .willReturn(aResponse().withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withStatus(200).withBodyFile("getAllOpenPullRequests.json")));
        wireMockServer.stubFor(post(
                urlEqualTo(String.format("/repositories/%s/%s/pullrequests/1/decline", TEST_OWNER, TEST_REPO_NAME)))
                .willReturn(aResponse().withStatus(200)));
        wireMockServer.stubFor(post(
                urlEqualTo(String.format("/repositories/%s/%s/pullrequests/1/comments", TEST_OWNER, TEST_REPO_NAME)))
                .willReturn(aResponse().withStatus(200)));
        wireMockServer.stubFor(get(urlEqualTo("/repositories?role=owner"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withStatus(200).withBodyFile("repos.json")));
    }

    @Test
    void makeSuccessfullPullRequest() {
        PullRequest pullRequest = PullRequest.builder()
                .repoOwner(TEST_OWNER)
                .repoName(TEST_REPO_NAME)
                .title(TEST_TITLE)
                .reviewer("reviewer_uuid")
                .branchName(TEST_BRANCH)
                .build();
        var response = bitBucketGitProvider.makePullRequest(pullRequest);
        assertThat(response)
                .isEqualToComparingFieldByField(PullRequestResult.builder()
                        .type("pullrequest")
                        .description("")
                        .link("https://bitbucket.org/grietvermeesch/testmavenproject/pull-requests/3")
                        .title("heyhey")
                        .id(3)
                        .state("OPEN")
                        .build());
    }

    @Test
    void makePullRequestWithoutReviewer(){
        PullRequest pullRequest = PullRequest.builder()
                .repoOwner(TEST_OWNER)
                .repoName(TEST_REPO_NAME)
                .title(TEST_TITLE)
                .branchName(TEST_BRANCH)
                .build();
        var response = bitBucketGitProvider.makePullRequest(pullRequest);
        assertThat(response)
                .isEqualToComparingFieldByField(PullRequestResult.builder()
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
        assertThatExceptionOfType(BitbucketNotFoundException.class)
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
        assertThatExceptionOfType(BitbucketBadRequestException.class)
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
        assertThatExceptionOfType(BitbucketUnauthorizedException.class)
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

    @Test
    void getOpenPullRequests(){
        assertThat(bitBucketGitProvider
                .getOpenPullRequests(TEST_OWNER, TEST_REPO_NAME))
                .hasSize(3);
    }

    @Test
    void closePullRequest(){
        PullRequest pullRequest = PullRequest.builder()
                .repoOwner(TEST_OWNER)
                .repoName(TEST_REPO_NAME)
                .title("Bumped org.hibernate:hibernate-core to version: 6.0.0.Alpha5")
                .branchName("autobump/org.hibernate/6.0.0.Alpha5")
                .pullRequestId(1)
                .build();
        assertThatCode(() -> bitBucketGitProvider
                .closePullRequest(pullRequest)).doesNotThrowAnyException();
    }

    @Test
    void commentPullRequest(){
        PullRequest pullRequest = PullRequest.builder()
                .repoOwner(TEST_OWNER)
                .repoName(TEST_REPO_NAME)
                .title("Bumped org.hibernate:hibernate-core to version: 6.0.0.Alpha5")
                .branchName("autobump/org.hibernate/6.0.0.Alpha5")
                .pullRequestId(1)
                .build();
        assertThatCode(() -> bitBucketGitProvider
                .commentPullRequest(pullRequest, "a comment")).doesNotThrowAnyException();
    }

    @Test
    void testCtorWithUser(){
        assertThat(new BitBucketGitProvider(new BitBucketAccount("glenn", "superSectret"))).isNotNull();
    }

    @Test
    void testCtorWithInterceptor(){
        assertThat(new BitBucketGitProvider(new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {

            }
        })).isNotNull();
    }

    @Test
    void testGetRepos() {
        assertThat(bitBucketGitProvider.getRepos().size()).isEqualTo(10);
    }

    @Test
    void getMembersFromWorkspace(){
        Repo repo = new Repo();
        repo.setName("grietvermeesch");
        repo.setLink("https://bitbucket.org/grietvermeesch/simplemultimoduletestproject/src/master/");
        wireMockServer.stubFor(get(urlEqualTo("/workspaces/master/members"))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withStatus(200)
                        .withBodyFile("members.json")));
        assertThat(bitBucketGitProvider.getMembersFromWorkspace(repo).size()).isEqualTo(3);
    }

    @Test
    void getCurrentUser(){
        wireMockServer.stubFor(get(urlEqualTo("/user")).willReturn(aResponse()
                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                .withStatus(200)
                .withBodyFile("user.json")));
        assertThat(bitBucketGitProvider.getCurrentUserUuid()).isEqualTo("{63738a1c-70ca-4e41-88fd-cd02a4c25c61}");
    }
}
