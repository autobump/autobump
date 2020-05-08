package com.github.autobump.core.model.usecases;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AutobumpUseCaseTest {

    public static final String REPOSITORY_URL = "http://www.test.test";
    public static final String TEST_NAME = "testName";
    private GitProvider gitProvider;
    private GitClient gitClient;
    private DependencyResolver dependencyResolver;
    private VersionRepository versionRepository;
    private DependencyBumper dependencyBumper;
    private UrlHelper urlHelper;
    private PullRequest pullRequest;
    private URI uri;

    @BeforeEach
    void setUp() throws URISyntaxException {
        makeMocks();
    }

    @SuppressWarnings("ExecutableStatementCount")
    private void setUpdoAutoBumpMocks() {
        Workspace workspace = new Workspace("");
        Mockito.when(gitClient.clone(uri)).thenReturn(workspace);
        TestVersion tv = new TestVersion("test");
        TestVersion tv1 = new TestVersion("bla");
        Dependency dependency1 = Dependency.builder().group("heyhey").name("test").version(tv).build();
        Dependency dependency2 = Dependency.builder().group("blabla").name("testo").version(tv).build();
        Dependency dependency3 = Dependency.builder().group("tadaa").name("bla").version(tv1).build();
        Mockito.when(dependencyResolver.resolve(workspace)).thenReturn(Set.of(dependency1, dependency2, dependency3));
        Mockito.when(versionRepository.getAllAvailableVersions(dependency1)).thenReturn(Set.of(tv));
        Mockito.when(versionRepository.getAllAvailableVersions(dependency3)).thenReturn(Set.of(tv));
        Mockito.when(versionRepository.getAllAvailableVersions(dependency2)).thenReturn(Set.of());
        Mockito.when(urlHelper.getOwnerName(REPOSITORY_URL)).thenReturn(TEST_NAME);
        Mockito.when(urlHelper.getRepoName(REPOSITORY_URL)).thenReturn(TEST_NAME);
        CommitResult commitResult = new CommitResult(TEST_NAME, "testMessage");
        Mockito.when(gitClient.commitToNewBranch(workspace,
                "heyhey",
                "test")).thenReturn(commitResult);
        Mockito.when(gitClient.commitToNewBranch(workspace,
                dependency3.getGroup(),
                dependency3.getVersion().getVersionNumber()))
                .thenReturn(commitResult);
        pullRequest = PullRequest.builder()
                .branchName(commitResult.getBranchName())
                .title(commitResult.getCommitMessage())
                .repoName(urlHelper.getRepoName(uri.toString()))
                .repoOwner(urlHelper.getOwnerName(uri.toString()))
                .build();
        Mockito.when(gitProvider.makePullRequest(pullRequest)).thenReturn(null);
    }

    private void makeMocks() throws URISyntaxException {
        uri = new URI(REPOSITORY_URL);
        urlHelper = Mockito.mock(UrlHelper.class);
        gitClient = Mockito.mock(GitClient.class);
        gitProvider = Mockito.mock(GitProvider.class);
        dependencyBumper = Mockito.mock(DependencyBumper.class);
        versionRepository = Mockito.mock(VersionRepository.class);
        dependencyResolver = Mockito.mock(DependencyResolver.class);
    }

    @Test
    void doAutoBump() {
        setUpdoAutoBumpMocks();
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

    @Test
    void doAutoBump_combinedDependencies() {
        setUpdoAutoBump_combinedDependenciesMocks();
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
        verify(gitProvider, times(1)).makePullRequest(pullRequest);
        assertThat(result.getNumberOfBumps()).isEqualTo(2);
    }

    @SuppressWarnings("ExecutableStatementCount")
    private void setUpdoAutoBump_combinedDependenciesMocks() {
        Workspace workspace = new Workspace("");
        Mockito.when(gitClient.clone(uri)).thenReturn(workspace);
        TestVersion tv = new TestVersion("test");
        Dependency dependency4 = Dependency.builder().group("same").name("testo").version(tv).build();
        Dependency dependency5 = Dependency.builder().group("same").name("bla").version(tv).build();
        Mockito.when(dependencyResolver.resolve(workspace)).thenReturn(Set.of(dependency4, dependency5));
        Mockito.when(versionRepository.getAllAvailableVersions(dependency4)).thenReturn(Set.of(tv));
        Mockito.when(versionRepository.getAllAvailableVersions(dependency5)).thenReturn(Set.of(tv));
        Mockito.when(urlHelper.getOwnerName(REPOSITORY_URL)).thenReturn(TEST_NAME);
        Mockito.when(urlHelper.getRepoName(REPOSITORY_URL)).thenReturn(TEST_NAME);
        CommitResult commitResult = new CommitResult(TEST_NAME, "testMessage");
        Mockito.when(gitClient.commitToNewBranch(workspace,
                "same",
                "test")).thenReturn(commitResult);
        Mockito.when(gitClient.commitToNewBranch(workspace,
                dependency4.getGroup(),
                dependency4.getVersion().getVersionNumber()))
                .thenReturn(commitResult);
        pullRequest = PullRequest.builder()
                .branchName(commitResult.getBranchName())
                .title(commitResult.getCommitMessage())
                .repoName(urlHelper.getRepoName(uri.toString()))
                .repoOwner(urlHelper.getOwnerName(uri.toString()))
                .build();
        Mockito.when(gitProvider.makePullRequest(pullRequest)).thenReturn(null);
    }

    private static class TestVersion implements Version {
        private final String version;

        TestVersion(String version) {
            this.version = version;
        }

        @Override
        public String getVersionNumber() {
            return version;
        }

        @Override
        public UpdateType getUpdateType(Version otherVersion) {
            return null;
        }

        @Override
        public int compareTo(Version o) {
            if (this.getVersionNumber().equals("bla")) {
                return -1;
            }
            return 1;
        }
    }
}
