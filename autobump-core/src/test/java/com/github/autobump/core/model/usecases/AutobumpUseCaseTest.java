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
import com.github.autobump.core.model.UseCaseConfiguration;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutobumpUseCaseTest {
    public static final String REPOSITORY_URL = "http://www.test.test";
    public static final String TEST_NAME = "testName";
    @Mock
    private GitProvider gitProvider;
    @Mock
    private GitClient gitClient;
    @Mock
    private DependencyResolver dependencyResolver;
    @Mock
    private VersionRepository versionRepository;
    @Mock
    private DependencyBumper dependencyBumper;
    @Mock
    private UrlHelper urlHelper;
    @Mock
    private PullRequest pullRequest;
    @Mock
    private IgnoreRepository ignoreRepository;
    private URI uri;
    private final Workspace workspace = new Workspace("");
    private TestVersion tv;
    private TestVersion tv1;
    private List<Dependency> dependencyList;
    private UseCaseConfiguration config;


    @BeforeEach
    void setUp() throws URISyntaxException {
        uri = new URI(REPOSITORY_URL);
        dependencyList = createDependencies();
        config = UseCaseConfiguration.builder()
                .ignoreRepository(ignoreRepository)
                .urlHelper(urlHelper)
                .versionRepository(versionRepository)
                .gitProvider(gitProvider)
                .dependencyResolver(dependencyResolver)
                .dependencyBumper(dependencyBumper)
                .gitClient(gitClient)
                .build();
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
        when(gitClient.clone(uri)).thenReturn(workspace);
        when(dependencyResolver.resolve(workspace))
                .thenReturn(Set.of(dependencyList.get(0), dependencyList.get(1), dependencyList.get(2)));
        when(versionRepository.getAllAvailableVersions(dependencyList.get(0))).thenReturn(Set.of(tv));
        when(versionRepository.getAllAvailableVersions(dependencyList.get(2))).thenReturn(Set.of(tv));
        when(versionRepository.getAllAvailableVersions(dependencyList.get(1))).thenReturn(Set.of());
        setupUrlHelper();
        setUpGitClassesMocks_forTestSingleBump();
    }

    private void setUpGitClassesMocks_forTestSingleBump() {
        CommitResult commitResult = new CommitResult(TEST_NAME, "testMessage");
        when(gitClient.commitToNewBranch(any(), any())).thenReturn(commitResult);
        pullRequest = PullRequest.builder()
                .branchName(commitResult.getBranchName())
                .title(commitResult.getCommitMessage())
                .repoName(urlHelper.getRepoName(uri.toString()))
                .repoOwner(urlHelper.getOwnerName(uri.toString())).build();
        Mockito.lenient().when(gitProvider.makePullRequest(pullRequest)).thenReturn(null);
        when(ignoreRepository.isIgnored(any(), any())).thenReturn(false);
    }

    @Test
    void doAutoBump() {
        setUpdoAutoBumpMocks_forTestSingleBump();
        var result = AutobumpUseCase.builder()
                .config(config)
                .uri(uri)
                .build()
                .doAutoBump();
        assertThat(result.getNumberOfBumps()).isEqualTo(1);
    }

    @Test
    void doAutoBump_combinedDependencies() {
        setUpdoAutoBump_combinedDependenciesMocks();
        var result = AutobumpUseCase.builder()
                .config(config)
                .uri(uri)
                .build()
                .doAutoBump();
        verify(gitProvider, times(1)).makePullRequest(any());
        assertThat(result.getNumberOfBumps()).isEqualTo(1);
    }

    @Test
    void doAutoBump_ignoreDependencyShouldnotBump() {
        setUpdoAutoBump_combinedDependenciesMocks();
        when(ignoreRepository.isIgnored(any(), any())).thenReturn(true);
        var result = AutobumpUseCase.builder()
                .config(config)
                .uri(uri)
                .build()
                .doAutoBump();
        assertThat(result.getNumberOfBumps()).isEqualTo(0);
    }

    private void setUpdoAutoBump_combinedDependenciesMocks() {
        when(gitClient.clone(uri)).thenReturn(workspace);
        when(dependencyResolver.resolve(workspace))
                .thenReturn(Set.of(dependencyList.get(3), dependencyList.get(4)));
        when(versionRepository.getAllAvailableVersions(dependencyList.get(3))).thenReturn(Set.of(tv));
        when(versionRepository.getAllAvailableVersions(dependencyList.get(4))).thenReturn(Set.of(tv));
        setupUrlHelper();
        CommitResult commitResult = new CommitResult(TEST_NAME, "testMessage");
        when(gitClient.commitToNewBranch(any(), any())).thenReturn(commitResult);
        setUpGitClassesMocks_forTestCombinedBumps();
    }

    private void setupUrlHelper() {
        when(urlHelper.getOwnerName(REPOSITORY_URL)).thenReturn(TEST_NAME);
        when(urlHelper.getRepoName(REPOSITORY_URL)).thenReturn(TEST_NAME);
    }

    private void setUpGitClassesMocks_forTestCombinedBumps() {
        CommitResult commitResult = new CommitResult(TEST_NAME, "testMessage");
        when(gitClient.commitToNewBranch(workspace,
                new Bump(dependencyList.get(3), tv))).thenReturn(commitResult);
        Mockito.lenient().when(gitClient.commitToNewBranch(workspace, new Bump(
                dependencyList.get(3),
                dependencyList.get(3).getVersion())))
                .thenReturn(commitResult);
        pullRequest = PullRequest.builder()
                .branchName(commitResult.getBranchName())
                .title(commitResult.getCommitMessage())
                .repoName(urlHelper.getRepoName(uri.toString()))
                .repoOwner(urlHelper.getOwnerName(uri.toString()))
                .build();
        Mockito.lenient().when(gitProvider.makePullRequest(pullRequest)).thenReturn(null);
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
