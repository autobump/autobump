package com.github.autobump.springboot.services;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.github.autobump.bitbucket.model.BitBucketGitProviderUrlHelper;
import com.github.autobump.core.model.dependencymanagement.DependencyBumper;
import com.github.autobump.core.model.dependencymanagement.DependencyResolver;
import com.github.autobump.core.model.domain.Repo;
import com.github.autobump.core.model.gitclients.GitClient;
import com.github.autobump.core.model.gitproviders.GitProvider;
import com.github.autobump.core.model.gitproviders.GitProviderUrlHelper;
import com.github.autobump.core.repositories.IgnoreRepository;
import com.github.autobump.core.repositories.VersionRepository;
import com.github.autobump.core.usecases.UseCaseConfiguration;
import com.github.autobump.springboot.configuration.Autobumpconfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith({SpringExtension.class, MockitoExtension.class, OutputCaptureExtension.class})
@SpringBootTest
@ActiveProfiles("noLogTest")
class AutoBumpServiceLoggerTest {
    @Autowired
    private AtlassianHostRepository repository;

    @Mock
    private Autobumpconfig autobumpconfig;

    @Autowired
    @InjectMocks
    private AutoBumpService testService;

    @Mock
    private GitProvider provider;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(provider.getRepos()).thenReturn(getDummyRepoList());
        Mockito.lenient().when(autobumpconfig.getGitProvider()).thenReturn(provider);
    }

    @Test
    void testSuccessfulNoLog(CapturedOutput log) {
        makeStubs();
        var host = new AtlassianHost();
        host.setClientKey("test");
        host.setSharedSecret("test");
        repository.save(host);
        testService.autoBump();
        assertThat(log).doesNotContain("bumped repo: test, number of bumps: 0");
    }

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
        Repo repo2 = new Repo();
        getRepo2(repos, repo2);
        return repos;
    }

    private void getRepo2(List<Repo> repos, Repo repo2) {
        repo2.setName("TestMavenProject");
        repo2.setSelected(false);
        repo2.setLink("another_link");
        repo2.setRepoId("emofbbSbgB");
        repos.add(repo2);
    }

    private Repo getRepo1() {
        Repo repo = new Repo();
        repo.setName("MultiModuleMavenProject");
        repo.setSelected(true);
        repo.setRepoId("cjhcvkjbub");
        repo.setLink("a_link");
        return repo;
    }
}
