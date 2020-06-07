package com.github.autobump.springboot.services;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith({SpringExtension.class, MockitoExtension.class, OutputCaptureExtension.class})
@SpringBootTest
@ActiveProfiles("test")
class AutoBumpServiceTest {
    /*@Autowired
    private AtlassianHostRepository repository;

    @Mock
    private Autobumpconfig autobumpconfig;

    @Mock
    private RepoRepository repoRepository;

    @Autowired
    @InjectMocks
    private AutoBumpService testService;

    @BeforeEach
    void setUp() {
        testService.setRepoRepository(repoRepository);
        var provider = Mockito.mock(GitProvider.class);
        lenient().when(provider.getRepos()).thenReturn(getDummyRepoList());
        lenient().when(autobumpconfig.getGitProvider()).thenReturn(provider);
    }

    private List<Repo> getDummyRepoList() {
        List<Repo> repos = new ArrayList<>();
        Repo repo = getRepo1();
        repos.add(repo);
        Repo repo2 = getRepo2();
        repos.add(repo2);
        return repos;
    }

    private Repo getRepo1() {
        Repo repo = new Repo();
        repo.setName("MultiModuleMavenProject");
        repo.setSelected(true);
        repo.setLink("a_link");
        repo.setRepoId("cjhcvkjbub");
        return repo;
    }

    private Repo getRepo2() {
        Repo repo2 = new Repo();
        repo2.setName("TestMavenProject");
        repo2.setSelected(false);
        repo2.setLink("another_link");
        repo2.setRepoId("emofbbSbgB");
        return repo2;
    }

    /*@Test
    void testRuntimeException(CapturedOutput log) {
        var host = new AtlassianHost();
        host.setClientKey("test");
        host.setSharedSecret("test");
        repository.save(host);
        testService.autoBump();
        assertThat(log).contains("Something went wrong while bumping: test");
    }

    @Test
    void testSuccessfulExecute(CapturedOutput log) {
        makeStubs();
        when(repoRepository.findAll()).thenReturn(getDummyRepoList());
        var host = new AtlassianHost();
        host.setClientKey("test");
        host.setSharedSecret("test");
        repository.save(host);
        testService.autoBump();
        assertThat(log).contains("bumped repo: test, number of bumps: 0");
    }

    private void makeStubs() {
        GitProviderUrlHelper urlHelper = Mockito.mock(BitBucketGitProviderUrlHelper.class);
        lenient().when(urlHelper.getOwnerName(any())).thenReturn("test");
        lenient().when(urlHelper.getRepoName(any())).thenReturn("test");
        lenient().when(autobumpconfig.setupConfig()).thenReturn(UseCaseConfiguration.builder()
                .gitProviderUrlHelper(urlHelper)
                .gitProvider(Mockito.mock(GitProvider.class))
                .versionRepository(Mockito.mock(VersionRepository.class))
                .ignoreRepository(Mockito.mock(IgnoreRepository.class))
                .gitClient(Mockito.mock(GitClient.class))
                .dependencyResolver(Mockito.mock(DependencyResolver.class))
                .dependencyBumper(Mockito.mock(DependencyBumper.class))
                .build());
    }*/
}
