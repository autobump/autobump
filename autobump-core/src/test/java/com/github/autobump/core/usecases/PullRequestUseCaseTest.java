package com.github.autobump.core.usecases;

import com.github.autobump.core.model.domain.Bump;
import com.github.autobump.core.model.domain.Dependency;
import com.github.autobump.core.model.domain.PullRequest;
import com.github.autobump.core.model.domain.Setting;
import com.github.autobump.core.model.domain.Version;
import com.github.autobump.core.model.domain.Workspace;
import com.github.autobump.core.model.gitclients.GitClient;
import com.github.autobump.core.model.gitproviders.GitProvider;
import com.github.autobump.core.model.gitproviders.GitProviderUrlHelper;
import com.github.autobump.core.model.results.CommitResult;
import com.github.autobump.core.model.results.PullRequestResult;
import com.github.autobump.core.repositories.SettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PullRequestUseCaseTest {
    private static final String PULL_REQUEST_TITLE_1
            = "Bumped com.h2database:h2 to version: 1.4.200";
    private static final String PULL_REQUEST_TITLE_2
            = "Bumped testGroup:testName to version: 6.1.0.jre7";
    private static final String PULL_REQUEST_TITLE_3
            = "PullRequest with title not referring to a bump";
    private static final String TEST_NAME = "testName";
    @Mock
    private GitProvider gitProvider;
    @Mock
    private GitClient gitClient;
    @Mock
    private GitProvider gitProviderThatReturnsEmptySetOfOpenPRs;
    @Mock
    private GitProvider gitProviderWithPrAlreadyInListOpenPullRequests;
    @Mock
    private GitProvider gitProviderThatReturnsNoPullRequestsToBeSuperseded;
    @Mock
    private GitProviderUrlHelper gitProviderUrlHelper;
    @Mock
    private SettingsRepository settingsRepository;
    @Mock
    private Workspace workspace;
    private URI uri;
    private Bump bump;

    @BeforeEach
    void setUp() throws URISyntaxException {
        createMocks();
        setUpMocks();
    }

    private void createMocks() throws URISyntaxException {
        uri = new URI("http://www.test.test");
        bump = new Bump(Dependency.builder()
                .group("testGroup")
                .name(TEST_NAME)
                .version(new TestVersion("testversion"))
                .build(),
                new TestVersion("heyhey"));
    }

    private void setUpMocks() {
        when(gitProviderUrlHelper.getOwnerName("http://www.test.test")).thenReturn(TEST_NAME);
        when(gitProviderUrlHelper.getRepoName("http://www.test.test")).thenReturn(TEST_NAME);
        CommitResult commitResult = new CommitResult(TEST_NAME, "testMessage");
        when(gitClient.commitToNewBranch(workspace, bump))
                .thenReturn(commitResult);
        PullRequest pullRequest = PullRequest.builder()
                .branchName(commitResult.getBranchName())
                .title(commitResult.getCommitMessage())
                .repoName(gitProviderUrlHelper.getRepoName(uri.toString()))
                .repoOwner(gitProviderUrlHelper.getOwnerName(uri.toString()))
                .reviewer("reviewer_name")
                .build();
        setUpGitProviders(pullRequest);
        when(settingsRepository.findSettingForReviewer(TEST_NAME)).thenReturn(getReviewerSetting());
    }

    private Setting getReviewerSetting() {
        Setting reviewerSetting = new Setting();
        reviewerSetting.setValue("reviewer_uuid");
        reviewerSetting.setKey("reviewer_name");
        return reviewerSetting;
    }

    private void setUpGitProviders(PullRequest pullRequest) {
        lenient().when(gitProvider.getOpenPullRequests(any(), any()))
                .thenReturn(createDummyPullRequests());
        lenient().when(gitProviderThatReturnsEmptySetOfOpenPRs.getOpenPullRequests(any(), any()))
                .thenReturn(Collections.emptySet());
        lenient().when(gitProviderWithPrAlreadyInListOpenPullRequests
                .getOpenPullRequests(any(), any()))
                .thenReturn(createDummyPullRequestsWithNewPr());
        lenient().when(gitProviderThatReturnsNoPullRequestsToBeSuperseded
                .getOpenPullRequests(any(), any()))
                .thenReturn(createDummyPullRequestNotToBeSupersededWithSameIdAsNewPullrequest());
        lenient().when(gitProvider.makePullRequest(any()))
                .thenReturn(createPullRequestResponse(pullRequest));
        lenient().when(gitProviderWithPrAlreadyInListOpenPullRequests.makePullRequest(any()))
                .thenReturn(createPullRequestResponse(pullRequest));
        lenient().when(gitProviderThatReturnsEmptySetOfOpenPRs.makePullRequest(any()))
                .thenReturn(createPullRequestResponse(pullRequest));
        lenient().when(gitProviderThatReturnsNoPullRequestsToBeSuperseded.makePullRequest(any()))
                .thenReturn(createPullRequestResponse(pullRequest));
    }

    private Set<PullRequest> createDummyPullRequestNotToBeSupersededWithSameIdAsNewPullrequest() {
        PullRequest pr = PullRequest.builder()
                .branchName("com.h2database:h2")
                .title(PULL_REQUEST_TITLE_1)
                .repoName("test")
                .repoOwner("test")
                .reviewer("reviewer_name")
                .pullRequestId(5)
                .build();
        return Set.of(pr);
    }

    private Set<PullRequest> createDummyPullRequestsWithNewPr() {
        Set<PullRequest> prs = createDummyPullRequests();
        prs.add(PullRequest.builder()
                .branchName("com.h2database:h2")
                .title(PULL_REQUEST_TITLE_1)
                .repoName("test")
                .repoOwner("test")
                .reviewer("reviewer_name")
                .pullRequestId(5)
                .build());
        return prs;
    }

    private PullRequestResult createPullRequestResponse(PullRequest pullRequest) {
        return PullRequestResult.builder()
                .type("PullRequest")
                .description("description")
                .link("a dummyLink")
                .title(pullRequest.getTitle())
                .id(5)
                .state("OPEN")
                .commentCount(0)
                .build();
    }

    @Test
    void doPullRequest_thatSupersedesOpenPullRequest() {
        assertThatCode(() ->
                PullRequestUseCase.builder()
                        .bump(bump)
                        .workspace(workspace)
                        .gitProviderUrlHelper(gitProviderUrlHelper)
                        .gitClient(gitClient)
                        .gitProvider(gitProvider)
                        .settingsRepository(settingsRepository)
                        .uri(uri)
                        .build()
                        .doPullRequest()
        ).doesNotThrowAnyException();
    }

    @Test
    void doPullRequest_thatIsAlreadyPresentInOpenPullRequests(){
        assertThatCode(() ->
                PullRequestUseCase.builder()
                        .bump(bump)
                        .workspace(workspace)
                        .gitProviderUrlHelper(gitProviderUrlHelper)
                        .gitClient(gitClient)
                        .settingsRepository(settingsRepository)
                        .gitProvider(gitProviderWithPrAlreadyInListOpenPullRequests)
                        .uri(uri)
                        .build()
                        .doPullRequest()
        ).doesNotThrowAnyException();
    }

    @Test
    void doPullrequest_withoutOpenPullRequests (){
        assertThatCode(() ->
                PullRequestUseCase.builder()
                        .bump(bump)
                        .workspace(workspace)
                        .gitProviderUrlHelper(gitProviderUrlHelper)
                        .gitClient(gitClient)
                        .gitProvider(gitProviderThatReturnsEmptySetOfOpenPRs)
                        .settingsRepository(settingsRepository)
                        .uri(uri)
                        .build()
                        .doPullRequest()
        ).doesNotThrowAnyException();
    }

    @Test
    void doPullrequest_withOpenPullRequestsThatDoesNotSupersede (){
        assertThatCode(() ->
                PullRequestUseCase.builder()
                        .bump(bump)
                        .workspace(workspace)
                        .gitProviderUrlHelper(gitProviderUrlHelper)
                        .gitClient(gitClient)
                        .settingsRepository(settingsRepository)
                        .gitProvider(gitProviderThatReturnsNoPullRequestsToBeSuperseded)
                        .uri(uri)
                        .build()
                        .doPullRequest()
        ).doesNotThrowAnyException();
    }

    private Set<PullRequest> createDummyPullRequests() {
        PullRequest pr1 = PullRequest.builder()
                .branchName("com.h2database:h2")
                .title(PULL_REQUEST_TITLE_1)
                .repoName("test")
                .repoOwner("test")
                .reviewer("reviewer_uuid")
                .pullRequestId(1)
                .build();
        PullRequest pr2 = PullRequest.builder()
                .branchName(TEST_NAME)
                .title(PULL_REQUEST_TITLE_2)
                .repoName(TEST_NAME)
                .repoOwner(TEST_NAME)
                .pullRequestId(2)
                .build();
        PullRequest pr3 = PullRequest.builder()
                .branchName("a developers branch")
                .title(PULL_REQUEST_TITLE_3)
                .repoName("test")
                .repoOwner("test")
                .pullRequestId(3)
                .build();
        Set<PullRequest> pullRequests = new java.util.HashSet<>();
        pullRequests.add(pr1);
        pullRequests.add(pr2);
        pullRequests.add(pr3);
        return pullRequests;
    }

    private static class TestVersion implements Version {
        private final String versionNumber;

        TestVersion(String versionNumber){
            this.versionNumber = versionNumber;
        }

        @Override
        public String getVersionNumber() {
            return versionNumber;
        }

        @Override
        public UpdateType getUpdateType(Version otherVersion) {
            return null;
        }

        @Override
        public int compareTo(Version o) {
            return 0;
        }
    }
}
