package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.UrlHelper;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RebaseUseCaseTest {
    public static final String REPOSITORY_URL = "http://www.test.test";
    private final static String pullRequestTitle1 = "Bumped com.h2database:h2 to version: 1.4.200";
    private final static String pullRequestTitle2 = "Bumped com.microsoft.sqlserver:mssql-jdbc to version: 6.1.0.jre7";
    private final static String pullRequestTitle3 = "PullRequest with title not referring to a bump";

    @Mock
    GitClient gitClient;
    @Mock
    GitProvider gitProvider;
    @Mock
    UrlHelper urlHelper;
    @InjectMocks
    RebaseUseCase rebaseUseCase;

    private final Workspace workspace = new Workspace("");

    @BeforeEach
    void setUp() throws URISyntaxException {
        rebaseUseCase = RebaseUseCase.builder().gitClient(gitClient).gitProvider(gitProvider).urlHelper(urlHelper).build();
        URI uri = new URI(REPOSITORY_URL);
        when(gitClient.clone(any())).thenReturn(workspace);
        when(urlHelper.getOwnerName(anyString())).thenReturn("test");
        when(urlHelper.getRepoName(anyString())).thenReturn("test");
        Set<PullRequest> prs = createDummyPullRequests();
        when(gitProvider.getOpenPullRequests("test", "test")).thenReturn(prs);
    }

    private Set<PullRequest> createDummyPullRequests() {
        PullRequest pr1 = PullRequest.builder()
                .branchName("com.h2database:h2")
                .title(pullRequestTitle1)
                .repoName("test")
                .repoOwner("test")
                .build();
        PullRequest pr2 = PullRequest.builder()
                .branchName("com.microsoft.sqlserver:mssql-jdbc")
                .title(pullRequestTitle2)
                .repoName("test")
                .repoOwner("test")
                .build();
        PullRequest pr3 = PullRequest.builder()
                .branchName("a developers branch")
                .title(pullRequestTitle3)
                .repoName("test")
                .repoOwner("test")
                .build();
        return Set.of(pr1, pr2, pr3);
    }

    @Test
    void handlePushEvent() throws URISyntaxException {
        assertThatCode(() -> rebaseUseCase.handlePushEvent(new PushEvent(new URI(""))))
                .doesNotThrowAnyException();
    }


}
