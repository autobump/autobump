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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PullRequestUseCaseTest {
    @Mock
    private GitProvider gitProvider;
    @Mock
    private GitClient gitClient;
    @Mock
    private UrlHelper urlHelper;
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
                .name("testName")
                .version(new TestVersion("testversion"))
                .build(),
                new TestVersion("heyhey"));
    }

    private void setUpMocks() {
        when(urlHelper.getOwnerName("http://www.test.test")).thenReturn("testName");
        when(urlHelper.getRepoName("http://www.test.test")).thenReturn("testName");
        CommitResult commitResult = new CommitResult("testName", "testMessage");
        when(gitClient.commitToNewBranch(workspace, bump))
                .thenReturn(commitResult);
        PullRequest pullRequest = PullRequest.builder()
                .branchName(commitResult.getBranchName())
                .title(commitResult.getCommitMessage())
                .repoName(urlHelper.getRepoName(uri.toString()))
                .repoOwner(urlHelper.getOwnerName(uri.toString()))
                .build();
        lenient().when(gitProvider.makePullRequest(pullRequest)).thenReturn(null);
    }

    @Test
    void doPullRequest() {
        assertThatCode(() ->
                PullRequestUseCase.builder()
                        .bump(bump)
                        .workspace(workspace)
                        .urlHelper(urlHelper)
                        .gitClient(gitClient)
                        .gitProvider(gitProvider)
                        .uri(uri)
                        .build()
                        .doPullRequest()
        ).doesNotThrowAnyException();
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
