package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.AutobumpResult;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoBumpSingleGroupUseCaseTest {
    public static final String REPOSITORY_URL = "http://www.test.test";
    private final Workspace workspace = new Workspace("");
    @Mock
    BumpResolverUseCase bumpResolverUseCase;
    @Mock
    BumpUseCase bumpUseCase;
    @Mock
    PushUseCase pushUseCase;
    @Mock
    private GitProvider gitProvider;
    @Mock
    private GitClient gitClient;
    @Mock
    private DependencyResolver dependencyResolver;
//    @Mock
//    private DependencyResolver dependencyResolver2;
//    @Mock
//    private VersionRepository versionRepository;
//    @Mock
//    private DependencyBumper dependencyBumper;
//    @Mock
//    private GitProviderUrlHelper gitProviderUrlHelper;
    private AutoBumpSingleGroupUseCase autoBumpSingleGroupUseCase;
//    @Mock
//    private IgnoreRepository ignoreRepository;
    private URI uri;
    private TestVersion tv;
    private PullRequest pullRequest;
    private Set<Dependency> dependencySet;
//    private UseCaseConfiguration config;

    @BeforeEach
    void setUp() throws URISyntaxException {
        pullRequest = PullRequest.builder()
                .pullRequestId(1)
                .branchName("testBranch")
                .repoName("testName")
                .repoOwner("testOwner")
                .title("Bumped org.hibernate:hibernate-core to version: 6.0.0.Alpha5")
                .build();
        uri = new URI(REPOSITORY_URL);
        dependencySet = createDependencies();
//        config = UseCaseConfiguration.builder()
//                .dependencyBumper(dependencyBumper)
//                .dependencyResolver(dependencyResolver)
//                .gitClient(gitClient)
//                .gitProvider(gitProvider)
//                .versionRepository(versionRepository)
//                .gitProviderUrlHelper(gitProviderUrlHelper)
//                .ignoreRepository(ignoreRepository)
//                .build();
        autoBumpSingleGroupUseCase = new AutoBumpSingleGroupUseCase(dependencyResolver,
                gitClient, gitProvider, bumpResolverUseCase, bumpUseCase, pushUseCase);
    }

    private Set<Dependency> createDependencies() {
        tv = new TestVersion("test");
        Set<Dependency> dependencies = new HashSet<>();
        dependencies.add(Dependency.builder().group("org.hibernate").name("test").version(tv).build());
        dependencies.add(Dependency.builder().group("org.hibernate").name("testo").version(tv).build());
        return dependencies;
    }

    private void setUpDependencyResolver() {
        when(dependencyResolver.resolve(workspace))
                .thenReturn(dependencySet);
//        when(versionRepository.getAllAvailableVersions(dependencyList.get(0))).thenReturn(Set.of(tv));
//        when(versionRepository.getAllAvailableVersions(dependencyList.get(1))).thenReturn(Set.of(tv));
        when(bumpResolverUseCase.doResolve(dependencySet)).thenReturn(Set.of(new Bump(dependencySet,tv)));
    }

//    private void setUpEmptyDependencyResolver() {
//        Mockito.lenient().when(dependencyResolver2.resolve(workspace))
//                .thenReturn(Collections.emptySet());
//    }

    @Test
    void doSingleGroupAutoBump() {
        setUpDependencyResolver();
        AutobumpResult result =
                autoBumpSingleGroupUseCase.doSingleGroupAutoBump(workspace, pullRequest);
                /* AutoBumpSingleGroupUseCase.builder()
                .
                .config(config)
                .build()
                .doSingleGroupAutoBump(uri, workspace, pullRequest);*/
        assertThat(result.getNumberOfBumps()).isEqualTo(1);
    }

//    @Test
//    void doSingleGroupAutoBump_WithoutDependenciesToUpdate() {
//        setUpEmptyDependencyResolver();
//        var result = AutoBumpSingleGroupUseCase.builder()
//                .config(config)
//                .build()
//                .doSingleGroupAutoBump(uri, workspace, pullRequest);
//        verify(gitProvider, times(1)).closePullRequest(pullRequest);
//        assertThat(result.getNumberOfBumps()).isEqualTo(0);
//    }

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
