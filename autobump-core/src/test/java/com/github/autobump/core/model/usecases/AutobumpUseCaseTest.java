package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.CommitResult;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.UrlHelper;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AutobumpUseCaseTest {

    private GitProvider gitProvider;

    private GitClient gitClient;

    private DependencyResolver dependencyResolver;

    private VersionRepository versionRepository;

    private DependencyBumper dependencyBumper;

    private UrlHelper urlHelper;

    private URI uri;

    @BeforeEach
    void setUp() throws URISyntaxException {
        makeMocks();
        setUpMocks();
    }
    @SuppressWarnings("ExecutableStatementCount")
    private void setUpMocks() {
        Workspace workspace = new Workspace("");
        Mockito.when(gitClient.clone(uri)).thenReturn(workspace);
        TestVersion tv = new TestVersion("test");
        TestVersion tv1 = new TestVersion("bla");
        Dependency dependency1 = Dependency.builder().name("test").version(tv).build();
        Dependency dependency2 = Dependency.builder().name("testo").version(tv).build();
        Dependency dependency3 = Dependency.builder().name("bla").version(tv1).build();
        Mockito.when(dependencyResolver.resolve(workspace)).thenReturn(Set.of(dependency1, dependency2, dependency3));
        Mockito.when(versionRepository.getAllAvailableVersions(dependency1)).thenReturn(Set.of(tv));
        Mockito.when(versionRepository.getAllAvailableVersions(dependency3)).thenReturn(Set.of(tv));
        Mockito.when(versionRepository.getAllAvailableVersions(dependency2)).thenReturn(Set.of());
        Mockito.when(urlHelper.getOwnerName("http://www.test.test")).thenReturn("testName");
        Mockito.when(urlHelper.getRepoName("http://www.test.test")).thenReturn("testName");
        CommitResult commitResult = new CommitResult("testName", "testMessage");
        Mockito.when(gitClient.commitToNewBranch(workspace, new Bump(dependency1, tv)))
                .thenReturn(commitResult);
        Mockito.when(gitClient.commitToNewBranch(workspace, new Bump(dependency3, tv)))
                .thenReturn(commitResult);
        PullRequest pullRequest = PullRequest.builder()
                .branchName(commitResult.getBranchName())
                .title(commitResult.getCommitMessage())
                .repoName(urlHelper.getRepoName(uri.toString()))
                .repoOwner(urlHelper.getOwnerName(uri.toString()))
                .build();
        Mockito.when(gitProvider.makePullRequest(pullRequest)).thenReturn(null);
    }

    private void makeMocks() throws URISyntaxException {
        uri = new URI("http://www.test.test");
        urlHelper = Mockito.mock(UrlHelper.class);
        gitClient = Mockito.mock(GitClient.class);
        gitProvider = Mockito.mock(GitProvider.class);
        dependencyBumper = Mockito.mock(DependencyBumper.class);
        versionRepository = Mockito.mock(VersionRepository.class);
        dependencyResolver = Mockito.mock(DependencyResolver.class);
    }

    @Test
    void doAutoBump() {
        var result = AutobumpUseCase.builder()
                .dependencyBumper(dependencyBumper)
                .dependencyResolver(dependencyResolver)
                .gitClient(gitClient)
                .gitProvider(gitProvider)
                .uri(uri)
                .urlHelper(urlHelper)
                .versionRepository(versionRepository)
                .build()
                .doAutoBump();
        assertThat(result.getNumberOfBumps()).isEqualTo(1);
    }

    private static class TestVersion implements Version {
        private final String version;

        TestVersion(String version) {
            this.version =version;
        }

        @Override
        public String getVersionNumber() {
            return version;
        }

        @Override
        public int compareTo(Version o) {
            if (this.getVersionNumber().equals("bla")){
                return -1;
            }
            return 1;
        }
    }
}