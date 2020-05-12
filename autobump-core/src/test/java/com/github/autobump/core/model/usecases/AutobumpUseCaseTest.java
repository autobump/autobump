package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.CommitResult;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.IgnoreRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
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
    private IgnoreRepository ignoreRepository;
    private URI uri;
    private final Workspace workspace = new Workspace("");
    private TestVersion tv;
    private TestVersion tv1;
    private List<Dependency> dependencyList;


    @BeforeEach
    void setUp() throws URISyntaxException {
        makeMocks();
        dependencyList = createDependencies();
    }

    private List<Dependency> createDependencies() {
        createTestVersions();
        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(Dependency.builder().group("heyhey").name("test").version(tv).build());
        dependencies.add(Dependency.builder().group("blabla").name("testo").version(tv).build());
        dependencies.add(Dependency.builder().group("tadaa").name("bla").version(tv1).build());
        dependencies.add(Dependency.builder().group("same").name("testo").version(tv).build());
        dependencies.add(Dependency.builder().group("same").name("bla").version(tv).build());
        return dependencies;
    }

    private void createTestVersions() {
        tv = new TestVersion("test");
        tv1 = new TestVersion("bla");
    }

    private void setUpdoAutoBumpMocks_forTestSingleBump() {
        Mockito.when(gitClient.clone(uri)).thenReturn(workspace);
        Mockito.when(dependencyResolver.resolve(workspace))
                .thenReturn(Set.of(dependencyList.get(0), dependencyList.get(1), dependencyList.get(2)));
        Mockito.when(versionRepository.getAllAvailableVersions(dependencyList.get(0))).thenReturn(Set.of(tv));
        Mockito.when(versionRepository.getAllAvailableVersions(dependencyList.get(2))).thenReturn(Set.of(tv));
        Mockito.when(versionRepository.getAllAvailableVersions(dependencyList.get(1))).thenReturn(Set.of());
        setupUrlHelper();
        setUpGitClassesMocks_forTestSingleBump();
    }

    private void setUpGitClassesMocks_forTestSingleBump() {
        CommitResult commitResult = new CommitResult(TEST_NAME, "testMessage");
        Mockito.when(gitClient.commitToNewBranch(anyObject(), anyObject())).thenReturn(commitResult);
        pullRequest = PullRequest.builder()
                .branchName(commitResult.getBranchName())
                .title(commitResult.getCommitMessage())
                .repoName(urlHelper.getRepoName(uri.toString()))
                .repoOwner(urlHelper.getOwnerName(uri.toString())).build();
        Mockito.when(gitProvider.makePullRequest(pullRequest)).thenReturn(null);
        Mockito.when(ignoreRepository.isIgnored(anyObject(), anyObject())).thenReturn(false);
    }

    private void makeMocks() throws URISyntaxException {
        uri = new URI(REPOSITORY_URL);
        urlHelper = Mockito.mock(UrlHelper.class);
        gitClient = Mockito.mock(GitClient.class);
        gitProvider = Mockito.mock(GitProvider.class);
        dependencyBumper = Mockito.mock(DependencyBumper.class);
        versionRepository = Mockito.mock(VersionRepository.class);
        dependencyResolver = Mockito.mock(DependencyResolver.class);
        ignoreRepository = Mockito.mock(IgnoreRepository.class);
    }

    @Test
    void doAutoBump() {
        setUpdoAutoBumpMocks_forTestSingleBump();
        var result = AutobumpUseCase.builder()
                .dependencyBumper(dependencyBumper)
                .dependencyResolver(dependencyResolver)
                .gitClient(gitClient)
                .gitProvider(gitProvider)
                .uri(uri)
                .urlHelper(urlHelper)
                .versionRepository(versionRepository)
                .ignoreRepository(ignoreRepository)
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
                .ignoreRepository(ignoreRepository)
                .build()
                .doAutoBump();
        verify(gitProvider, times(1)).makePullRequest(any());
        assertThat(result.getNumberOfBumps()).isEqualTo(1);
    }

    @Test
    void doAutoBump_ignoreDependencyShouldnotBump() {
        setUpdoAutoBump_combinedDependenciesMocks();
        Mockito.when(ignoreRepository.isIgnored(any(), any())).thenReturn(true);
        var result = AutobumpUseCase.builder()
                .dependencyBumper(dependencyBumper)
                .dependencyResolver(dependencyResolver)
                .gitClient(gitClient)
                .gitProvider(gitProvider)
                .uri(uri)
                .urlHelper(urlHelper)
                .versionRepository(versionRepository)
                .ignoreRepository(ignoreRepository)
                .build()
                .doAutoBump();
        assertThat(result.getNumberOfBumps()).isEqualTo(0);
    }

    private void setUpdoAutoBump_combinedDependenciesMocks() {
        Mockito.when(gitClient.clone(uri)).thenReturn(workspace);
        Mockito.when(dependencyResolver.resolve(workspace))
                .thenReturn(Set.of(dependencyList.get(3), dependencyList.get(4)));
        Mockito.when(versionRepository.getAllAvailableVersions(dependencyList.get(3))).thenReturn(Set.of(tv));
        Mockito.when(versionRepository.getAllAvailableVersions(dependencyList.get(4))).thenReturn(Set.of(tv));
        setupUrlHelper();
        CommitResult commitResult = new CommitResult(TEST_NAME, "testMessage");
        Mockito.when(gitClient.commitToNewBranch(any(), any())).thenReturn(commitResult);
        setUpGitClassesMocks_forTestCombinedBumps();
    }

    private void setupUrlHelper() {
        Mockito.when(urlHelper.getOwnerName(REPOSITORY_URL)).thenReturn(TEST_NAME);
        Mockito.when(urlHelper.getRepoName(REPOSITORY_URL)).thenReturn(TEST_NAME);
    }

    private void setUpGitClassesMocks_forTestCombinedBumps() {
        CommitResult commitResult = new CommitResult(TEST_NAME, "testMessage");
        Mockito.when(gitClient.commitToNewBranch(workspace,
                new Bump(dependencyList.get(3), tv))).thenReturn(commitResult);
        Mockito.when(gitClient.commitToNewBranch(workspace, new Bump(
                dependencyList.get(3),
                dependencyList.get(3).getVersion())))
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
