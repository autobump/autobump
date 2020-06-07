package com.github.autobump.springboot.services;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.github.autobump.bitbucket.model.BitBucketGitProviderUrlHelper;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.GitProviderUrlHelper;
import com.github.autobump.core.model.IgnoreRepository;
import com.github.autobump.core.model.Repo;
import com.github.autobump.core.model.RepoRepository;
import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.core.model.UseCaseConfiguration;
import com.github.autobump.core.model.VersionRepository;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class, OutputCaptureExtension.class})
@SpringBootTest
@ActiveProfiles("test")
class AutoBumpServiceTest {

    @Mock
    private SettingsRepository settingsRepository;

    @Autowired
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
        when(settingsRepository.getCronSetting(any())).thenReturn(Setting.builder().value("gdsv").key("hdjsbfjl")
                .repositoryName("dsffd").type(Setting.SettingsType.CRON).build());
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

    @Test
    void testRuntimeException(CapturedOutput log) {
        when(repoRepository.findAll()).thenReturn(getDummyRepoList());
        var host = new AtlassianHost();
        host.setClientKey("test");
        host.setSharedSecret("test");
        repository.save(host);
        testService.autoBump();
        assertThat(log).contains("Something went wrong while bumping: a_link");
    }

    @Test
    void testSuccessfulExecute() {
        makeStubs();
        when(repoRepository.findAll()).thenReturn(getDummyRepoList());
        var host = new AtlassianHost();
        host.setClientKey("test");
        host.setSharedSecret("test");
        repository.save(host);
        assertThatCode(() -> testService.autoBump()).doesNotThrowAnyException();
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
    }
}
