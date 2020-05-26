package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.AutoBumpRebaseResult;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.IgnoreRepository;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.UrlHelper;
import com.github.autobump.core.model.UseCaseConfiguration;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.core.model.events.PushEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

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
    UrlHelper urlHelper;

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
        lenient().when(gitClientWithoutConflict.clone(any())).thenReturn(workspace);
        lenient().when(gitClientWithoutConflict.rebaseBranchFromMaster(any(), any()))
                .thenReturn(new AutoBumpRebaseResult(false));
        lenient().when(gitClientWithConflict.rebaseBranchFromMaster(any(), any()))
                .thenReturn(new AutoBumpRebaseResult(true));
        when(urlHelper.getOwnerName(anyString())).thenReturn("test");
        when(urlHelper.getRepoName(anyString())).thenReturn("test");
        Set<PullRequest> prs = createDummyPullRequests();
        when(gitProvider.getOpenPullRequests("test", "test"))
                .thenReturn(prs);
    }

    private void createRebaseUseCases() throws URISyntaxException {
        UseCaseConfiguration configWithoutConflict = UseCaseConfiguration.builder()
                .gitClient(gitClientWithoutConflict)
                .dependencyBumper(dependencyBumper)
                .dependencyResolver(dependencyResolver)
                .gitProvider(gitProvider)
                .ignoreRepository(ignoreRepository)
                .urlHelper(urlHelper)
                .versionRepository(versionRepository)
                .build();

        rebaseUseCaseWithoutConflict = RebaseUseCase.builder()
                .config(configWithoutConflict)
                .event(new PushEvent(new URI("")))
                .build();

        UseCaseConfiguration configWithConflict = UseCaseConfiguration.builder()
                .gitClient(gitClientWithConflict)
                .dependencyBumper(dependencyBumper)
                .dependencyResolver(dependencyResolver)
                .gitProvider(gitProvider)
                .ignoreRepository(ignoreRepository)
                .urlHelper(urlHelper)
                .versionRepository(versionRepository)
                .build();

        rebaseUseCaseWithConflict = RebaseUseCase.builder()
                .config(configWithConflict)
                .event(new PushEvent(new URI("")))
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
                .handlePushEvent())
                .doesNotThrowAnyException();
    }

    @Test
    void handlePushEventWithConflicts()  {
        assertThatCode(() -> rebaseUseCaseWithConflict
                .handlePushEvent())
                .doesNotThrowAnyException();
    }
}
