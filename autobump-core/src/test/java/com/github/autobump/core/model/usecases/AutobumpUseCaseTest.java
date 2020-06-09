package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.CommitResult;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.GitProviderUrlHelper;
import com.github.autobump.core.model.IgnoreRepository;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.PullRequestResponse;
import com.github.autobump.core.model.ReleaseNotesSource;
import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.core.model.UseCaseConfiguration;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.TooManyFields")
@ExtendWith(MockitoExtension.class)
class AutobumpUseCaseTest {
    private static final String REPOSITORY_URL = "http://www.test.test";
    private static final String TEST_NAME = "testName";
    private static final String TEST_PROJ_URL = "https://github.com/testprojecturl";
    private static final String TEST_REL_NOTES_CONTENT = "release notes content";
    private final Workspace workspace = new Workspace("");
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
    private GitProviderUrlHelper gitProviderUrlHelper;
    @Mock
    private PullRequest pullRequest;
    @Mock
    private IgnoreRepository ignoreRepository;
    @Mock
    private ReleaseNotesSource releaseNotesSource;
    @Mock
    private SettingsRepository settingsRepository;
    private URI uri;
    private TestVersion tv;
    private TestVersion tv1;
    private List<Dependency> dependencyList;
    private UseCaseConfiguration config;
    private int prResponseCommentCount;

    @BeforeEach
    void setUp() throws URISyntaxException {
        uri = new URI(REPOSITORY_URL);
        dependencyList = createDependencies();
        config = UseCaseConfiguration.builder()
                .ignoreRepository(ignoreRepository)
                .gitProviderUrlHelper(gitProviderUrlHelper)
                .versionRepository(versionRepository)
                .gitProvider(gitProvider)
                .dependencyResolver(dependencyResolver)
                .dependencyBumper(dependencyBumper)
                .gitClient(gitClient)
                .build();
        prResponseCommentCount = 0;
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
                .title("Bumped heyhey:test to version: test")
                .repoName(gitProviderUrlHelper.getRepoName(uri.toString()))
                .repoOwner(gitProviderUrlHelper.getOwnerName(uri.toString())).build();
        lenient().when(gitProvider.makePullRequest(pullRequest))
                .thenReturn(createPullRequestResponse(pullRequest));
        when(ignoreRepository.isIgnored(any(), any())).thenReturn(false);
    }

    @Test
    void doAutoBump() {
        when(gitProvider.makePullRequest(any())).thenReturn(PullRequestResponse.builder().id(5).build());
        setUpdoAutoBumpMocks_forTestSingleBump();
        var result = buildAutobumpUseCase().doAutoBump();
        assertThat(result.getNumberOfBumps()).isEqualTo(1);
    }

    @Test
    void doAutoBump_combinedDependencies() {
        setUpdoAutoBump_combinedDependenciesMocks();
        var result = buildAutobumpUseCase().doAutoBump();
        verify(gitProvider, times(1)).makePullRequest(any());
        assertThat(result.getNumberOfBumps()).isEqualTo(1);
    }

    @Test
    void doAutoBump_ignoreDependencyShouldNotBump() {
        setUpdoAutoBump_combinedDependenciesMocks();
        when(ignoreRepository.isIgnored(any(), any())).thenReturn(true);
        var result = buildAutobumpUseCase().doAutoBump();
        assertThat(result.getNumberOfBumps()).isEqualTo(0);
    }

    private AutobumpUseCase buildAutobumpUseCase() {
        Setting reviewerSetting = new Setting();
        reviewerSetting.setValue("reviewer_uuid");
        reviewerSetting.setKey("reviewer_name");
        lenient().when(settingsRepository.findSettingForReviewer(TEST_NAME)).thenReturn(reviewerSetting);
        return AutobumpUseCase.builder()
                .config(config)
                .uri(uri)
                .releaseNotesSource(releaseNotesSource)
                .settingsRepository(settingsRepository)
                .build();
    }

    @Test
    void postCommentOnPullRequest_validCommentCreatesValidPostCommentOnPullRequestUseCase() {
        setUpdoAutoBump_combinedDependenciesMocks();
        setupAvailableReleaseNotesMocks();
        buildAutobumpUseCase().doAutoBump();
        verify(gitProvider, times(1)).commentPullRequest(any(), contains(TEST_REL_NOTES_CONTENT));
    }

    @Test
    void postCommentOnPullRequest_blankCommentNoPostCommentOnPullRequestUseCase() {
        setUpdoAutoBump_combinedDependenciesMocks();
        when(versionRepository.getScmUrlForDependencyVersion(any(), any()))
                .thenReturn(TEST_PROJ_URL);
        when(releaseNotesSource.getReleaseNotes(eq(TEST_PROJ_URL), any()))
                .thenReturn(new ReleaseNotes(TEST_PROJ_URL + "/tags/1.0"
                        , "1.0", ""));
        buildAutobumpUseCase().doAutoBump();
        verify(gitProvider, times(0)).commentPullRequest(any(), contains(TEST_REL_NOTES_CONTENT));
    }

    @Test
    void postCommentOnPullRequest_nullReleaseNotesNoPostCommentOnPullRequestUseCase() {
        setUpdoAutoBump_combinedDependenciesMocks();
        when(versionRepository.getScmUrlForDependencyVersion(any(), any()))
                .thenReturn(TEST_PROJ_URL);
        when(releaseNotesSource.getReleaseNotes(eq(TEST_PROJ_URL), any()))
                .thenReturn(null);
        buildAutobumpUseCase().doAutoBump();
        verify(gitProvider, times(0)).commentPullRequest(any(), contains(TEST_REL_NOTES_CONTENT));
    }

    @Test
    void postCommentOnPullRequest_DontPostCommentOnOlderPullRequest() {
        prResponseCommentCount = 1;
        setUpdoAutoBump_combinedDependenciesMocks();
        setupAvailableReleaseNotesMocks();
        buildAutobumpUseCase().doAutoBump();
        verify(gitProvider, times(0)).commentPullRequest(any(), contains(TEST_REL_NOTES_CONTENT));
    }

    private void setupAvailableReleaseNotesMocks() {
        when(versionRepository.getScmUrlForDependencyVersion(any(), any()))
                .thenReturn(TEST_PROJ_URL);
        when(releaseNotesSource.getReleaseNotes(eq(TEST_PROJ_URL), any()))
                .thenReturn(new ReleaseNotes(TEST_PROJ_URL + "/tags/1.0"
                        , "1.0", TEST_REL_NOTES_CONTENT));
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
        when(gitProviderUrlHelper.getOwnerName(REPOSITORY_URL)).thenReturn(TEST_NAME);
        when(gitProviderUrlHelper.getRepoName(REPOSITORY_URL)).thenReturn(TEST_NAME);
    }

    private void setUpGitClassesMocks_forTestCombinedBumps() {
        CommitResult commitResult = new CommitResult(TEST_NAME, "testMessage");
        when(gitClient.commitToNewBranch(workspace,
                new Bump(dependencyList.get(3), tv))).thenReturn(commitResult);
        lenient().when(gitClient.commitToNewBranch(workspace, new Bump(
                dependencyList.get(3),
                dependencyList.get(3).getVersion())))
                .thenReturn(commitResult);
        pullRequest = PullRequest.builder()
                .branchName(commitResult.getBranchName())
                .title("Bumped same:testo and same:bla to version: test")
                .repoName(gitProviderUrlHelper.getRepoName(uri.toString()))
                .repoOwner(gitProviderUrlHelper.getOwnerName(uri.toString()))
                .build();
        PullRequestResponse response = createPullRequestResponse(pullRequest);
        lenient().when(gitProvider.makePullRequest(any()))
                .thenReturn(response);
    }

    private PullRequestResponse createPullRequestResponse(PullRequest pullRequest) {
        return PullRequestResponse.builder()
                .type("PullRequest")
                .description("description")
                .link("a dummyLink")
                .title(pullRequest.getTitle())
                .id(5)
                .state("OPEN")
                .commentCount(prResponseCommentCount)
                .build();
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
