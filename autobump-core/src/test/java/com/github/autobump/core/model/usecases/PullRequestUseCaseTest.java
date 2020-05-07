package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.CommitResult;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.UrlHelper;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.net.URISyntaxException;

class PullRequestUseCaseTest {
    private GitProvider gitProvider;
    private GitClient gitClient;
    private UrlHelper urlHelper;
    private Workspace workspace;
    private Bump bump;
    private URI uri;

    @BeforeEach
    void setUp() throws URISyntaxException {
        createMocks();
        setUpMocks();
    }

    private void createMocks() throws URISyntaxException {
        workspace = Mockito.mock(Workspace.class);
        bump = new Bump(Dependency.builder().build(), new TestVersion());
        uri = new URI("http://www.test.test");
        urlHelper = Mockito.mock(UrlHelper.class);
        gitClient = Mockito.mock(GitClient.class);
        gitProvider = Mockito.mock(GitProvider.class);
    }

    private void setUpMocks() {
        Mockito.when(urlHelper.getOwnerName("http://www.test.test")).thenReturn("testName");
        Mockito.when(urlHelper.getRepoName("http://www.test.test")).thenReturn("testName");
        CommitResult commitResult = new CommitResult("testName", "testMessage");
        Mockito.when(gitClient.commitToNewBranch(workspace, bump))
                .thenReturn(commitResult);
        PullRequest pullRequest = PullRequest.builder()
                .branchName(commitResult.getBranchName())
                .title(commitResult.getCommitMessage())
                .repoName(urlHelper.getRepoName(uri.toString()))
                .repoOwner(urlHelper.getOwnerName(uri.toString()))
                .build();
        Mockito.when(gitProvider.makePullRequest(pullRequest)).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void doPullRequest() {
        PullRequestUseCase.builder()
                .bump(bump)
                .workspace(workspace)
                .urlHelper(urlHelper)
                .gitClient(gitClient)
                .gitProvider(gitProvider)
                .uri(uri)
                .build()
                .doPullRequest();
    }

    private static class TestVersion implements Version{

        @Override
        public String getVersionNumber() {
            return null;
        }

        @Override
        public int compareTo(Version o) {
            return 0;
        }
    }
}
