package com.github.autobump.springboot.services;

import com.atlassian.connect.spring.AtlassianHostRepository;
import com.github.autobump.bitbucket.model.BitBucketGitProviderUrlHelper;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.GitProviderUrlHelper;
import com.github.autobump.core.model.IgnoreRepository;
import com.github.autobump.core.model.Repo;
import com.github.autobump.core.model.UseCaseConfiguration;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.springboot.configuration.Autobumpconfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith({SpringExtension.class, MockitoExtension.class, OutputCaptureExtension.class})
@SpringBootTest
@ActiveProfiles("test")
class AutoBumpServiceTest {
    @Autowired
    private AtlassianHostRepository repository;

    @Mock
    private Autobumpconfig autobumpconfig;

    @Autowired
    @InjectMocks
    private AutoBumpService testService;

    @BeforeEach
    void setUp() {
        var provider = Mockito.mock(GitProvider.class);
        Mockito.lenient().when(provider.getRepos()).thenReturn(getDummyRepoList());
        Mockito.lenient().when(autobumpconfig.getGitProvider()).thenReturn(provider);
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
        var host = new AtlassianHost();
        host.setClientKey("test");
        host.setSharedSecret("test");
        repository.save(host);
        testService.autoBump();
        assertThat(log).contains("bumped repo: test, number of bumps: 0");
    }*/

    private void makeStubs() {
        GitProviderUrlHelper urlHelper = Mockito.mock(BitBucketGitProviderUrlHelper.class);
        Mockito.lenient().when(urlHelper.getOwnerName(any())).thenReturn("test");
        Mockito.lenient().when(urlHelper.getRepoName(any())).thenReturn("test");
        Mockito.lenient().when(autobumpconfig.setupConfig()).thenReturn(UseCaseConfiguration.builder()
                .gitProviderUrlHelper(urlHelper)
                .gitProvider(Mockito.mock(GitProvider.class))
                .versionRepository(Mockito.mock(VersionRepository.class))
                .ignoreRepository(Mockito.mock(IgnoreRepository.class))
                .gitClient(Mockito.mock(GitClient.class))
                .dependencyResolver(Mockito.mock(DependencyResolver.class))
                .dependencyBumper(Mockito.mock(DependencyBumper.class))
                .build());
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
}
