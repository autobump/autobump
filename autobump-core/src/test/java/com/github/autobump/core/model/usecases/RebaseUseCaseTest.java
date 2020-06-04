package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.AutoBumpRebaseResult;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.GitProviderUrlHelper;
import com.github.autobump.core.model.IgnoreRepository;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.UseCaseConfiguration;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.core.model.events.PushEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RebaseUseCaseTest {
    public static final String REPOSITORY_URL = "http://www.test.test";
    private static final String PULL_REQUEST_TITLE_1
            = "Bumped com.h2database:h2 to version: 1.4.200";
    private static final String PULL_REQUEST_TITLE_2
            = "Bumped com.microsoft.sqlserver:mssql-jdbc to version: 6.1.0.jre7";
    private static final String PULL_REQUEST_TITLE_3
            = "PullRequest with title not referring to a bump";

    @Mock
    GitClient gitClientWithoutConflict;

    @Mock
    GitClient gitClientWithConflict;

    @Mock
    GitProvider gitProvider;

    @Mock
    DependencyResolver dependencyResolver;

    @Mock
    IgnoreRepository ignoreRepository;

    @Mock
    DependencyBumper dependencyBumper;

    @Mock
    GitProviderUrlHelper gitProviderUrlHelper;

    @Mock
    VersionRepository versionRepository;

    @InjectMocks
    RebaseUseCase rebaseUseCaseWithoutConflict;

    @InjectMocks
    RebaseUseCase rebaseUseCaseWithConflict;

    private final Workspace workspace = new Workspace("");

    @BeforeEach
    void setUp() throws URISyntaxException {
        createRebaseUseCases();
        setupGitClientMocks();
        setupUrlHelperMocks();
        setupPullRequestMocks();
    }

    private void setupPullRequestMocks() {
        Set<PullRequest> prs = createDummyPullRequests();
        lenient().when(gitProvider.getOpenPullRequests("test", "test"))
                .thenReturn(prs);
    }

    private void setupUrlHelperMocks() {
        lenient().when(gitProviderUrlHelper.getOwnerName(anyString())).thenReturn("test");
        lenient().when(gitProviderUrlHelper.getRepoName(anyString())).thenReturn("test");
    }

    private void setupGitClientMocks() {
        lenient().when(gitClientWithoutConflict.clone(any())).thenReturn(workspace);
        lenient().when(gitClientWithoutConflict.rebaseBranchFromMaster(any(), any()))
                .thenReturn(new AutoBumpRebaseResult(false));
        lenient().when(gitClientWithConflict.clone(any())).thenReturn(workspace);
        lenient().when(gitClientWithConflict.rebaseBranchFromMaster(any(), any()))
                .thenReturn(new AutoBumpRebaseResult(true));
    }

    private void createRebaseUseCases() throws URISyntaxException {
        UseCaseConfiguration configWithoutConflict = UseCaseConfiguration.builder()
                .gitClient(gitClientWithoutConflict)
                .dependencyBumper(dependencyBumper)
                .dependencyResolver(dependencyResolver)
                .gitProvider(gitProvider)
                .ignoreRepository(ignoreRepository)
                .gitProviderUrlHelper(gitProviderUrlHelper)
                .versionRepository(versionRepository)
                .build();

        rebaseUseCaseWithoutConflict = RebaseUseCase.builder()
                .config(configWithoutConflict)
                .build();

        UseCaseConfiguration configWithConflict = UseCaseConfiguration.builder()
                .gitClient(gitClientWithConflict)
                .dependencyBumper(dependencyBumper)
                .dependencyResolver(dependencyResolver)
                .gitProvider(gitProvider)
                .ignoreRepository(ignoreRepository)
                .gitProviderUrlHelper(gitProviderUrlHelper)
                .versionRepository(versionRepository)
                .build();

        rebaseUseCaseWithConflict = RebaseUseCase.builder()
                .config(configWithConflict)
                .build();
    }

    private Set<PullRequest> createDummyPullRequests() {
        PullRequest pr1 = PullRequest.builder()
                .branchName("com.h2database:h2")
                .title(PULL_REQUEST_TITLE_1)
                .repoName("test")
                .repoOwner("test")
                .pullRequestId(1)
                .build();
        PullRequest pr2 = PullRequest.builder()
                .branchName("com.microsoft.sqlserver:mssql-jdbc")
                .title(PULL_REQUEST_TITLE_2)
                .repoName("test")
                .repoOwner("test")
                .pullRequestId(2)
                .build();
        PullRequest pr3 = PullRequest.builder()
                .branchName("a developers branch")
                .title(PULL_REQUEST_TITLE_3)
                .repoName("test")
                .repoOwner("test")
                .pullRequestId(3)
                .build();
        return Set.of(pr1, pr2, pr3);
    }

    @Test
    void handlePushEventWithoutConflicts() {
        assertThatCode(() -> rebaseUseCaseWithoutConflict
                .handlePushEvent(new PushEvent(new URI(""))))
                .doesNotThrowAnyException();
    }

    @Test
    void handlePushEventWithConflicts() {
        assertThatCode(() -> rebaseUseCaseWithConflict
                .handlePushEvent(new PushEvent(new URI(""))))
                .doesNotThrowAnyException();
    }

    @Test
    void testPrEmpty() {
        var provider = Mockito.mock(GitProvider.class);
        var providerUrlHelper = Mockito.mock(GitProviderUrlHelper.class);
        var config = UseCaseConfiguration.builder()
                .dependencyBumper(Mockito.mock(DependencyBumper.class))
                .dependencyResolver(Mockito.mock(DependencyResolver.class))
                .gitClient(Mockito.mock(GitClient.class))
                .ignoreRepository(Mockito.mock(IgnoreRepository.class))
                .gitProvider(provider)
                .gitProviderUrlHelper(providerUrlHelper)
                .versionRepository(Mockito.mock(VersionRepository.class))
                .build();
        Mockito.when(provider.getOpenPullRequests(any(), any())).thenReturn(Set.of());
        assertThatCode(() -> RebaseUseCase.builder()
                .config(config)
                .build()
                .handlePushEvent(new PushEvent(new URI(""))))
                .doesNotThrowAnyException();
    }
}
