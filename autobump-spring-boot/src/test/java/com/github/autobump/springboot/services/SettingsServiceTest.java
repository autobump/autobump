package com.github.autobump.springboot.services;

import com.atlassian.connect.spring.AtlassianHostRepository;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.Setting;
import com.github.autobump.springboot.configuration.Autobumpconfig;
import com.github.autobump.springboot.controllers.dtos.DependencyDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import com.github.autobump.springboot.domain.Repo;
import com.github.autobump.springboot.repositories.RepoRepository;
import com.github.autobump.springboot.repositories.SpringSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
class SettingsServiceTest {
    public static final String REPOSITORY_NAME = "TestMavenProject";

    @Autowired
    AtlassianHostRepository atlassianHostRepository;

    @Mock
    ModelMapper modelMapper;

    @Mock
    SpringSettingsRepository springSettingsRepository;

    @Mock
    RepoRepository repoRepository;

    @Mock
    Autobumpconfig autobumpconfig;

    @Mock
    GitProvider gitProvider;

    @Autowired
    SettingsService service;

    List<Repo> dummyRepos;

    RepositoryDto dummyRepoDto;


    @BeforeEach
    void setUp() {
        service = new SettingsService();
        service.setRepoRepository(repoRepository);
        service.setModelMapper(modelMapper);
        service.setSettingsRepository(springSettingsRepository);
        dummyRepos = getDummyRepoList();
        List<DependencyDto> expectedDependencyList = new ArrayList<>();
        expectedDependencyList.add(DependencyDto.builder()
                .groupName("org.projectlombok")
                .artifactId("lombok")
                .versionNumber("1.18.12")
                .ignoreMajor(true)
                .ignoreMinor(false)
                .build());
        dummyRepoDto = new RepositoryDto();
        dummyRepoDto.setName(REPOSITORY_NAME);
        dummyRepoDto.setCronJob(true);
        dummyRepoDto.setReviewer("name of a reviewer");
        dummyRepoDto.setDependencies(expectedDependencyList);
    }

    @Test
    void getAllRepositories() {
        //when(autobumpconfig.getGitProvider()).thenReturn(gitProvider);
        // TODO - fix gitprovider first
    }

    @Test
    void getMonitoredRepos() {
        when(repoRepository.findAll())
                .thenReturn(dummyRepos);
        when(modelMapper.map(any(Repo.class), any()))
                .thenReturn(new RepositoryDto());
        assertThat(service.getMonitoredRepos().size()).isEqualTo(1);
    }

    @Test
    void getSettingsForRepository() {
        when(springSettingsRepository.findAllSettingsForDependencies(REPOSITORY_NAME))
                .thenReturn(getDummySettings());
        assertThat(service.getSettingsForRepository(REPOSITORY_NAME)).isEqualTo(dummyRepoDto);
    }

    @Test
    void doAutoBump() {
    }

    @Test
    void getRepository() {
    }

    @Test
    void updateRepo() {

    }

    @Test
    void saveSettings() {
    }



    private List<Repo> getDummyRepoList() {
        List<Repo> repos = new ArrayList<>();
        Repo repo = new Repo();
        repo.setName("MultiModuleMavenProject");
        repo.setSelected(true);
        repo.setRepoId(1);
        repos.add(repo);
        Repo repo2 = new Repo();
        repo2.setName("TestMavenProject");
        repo2.setSelected(false);
        repo2.setRepoId(2);
        repos.add(repo2);
        return repos;
    }

    private List<Setting> getDummySettings(){
        List<Setting> dummies = new ArrayList<Setting>();
        Setting s1 = Setting.builder()
                .repositoryName(REPOSITORY_NAME)
                .key("org.projectlombok:lombok:1.18.12")
                .type(Setting.SettingsType.IGNORE)
                .value("major")
                .build();
        dummies.add(s1);
        Setting s2 = Setting.builder()
                .key("reviewer")
                .type(Setting.SettingsType.REVIEWER)
                .value("name of a reviewer")
                .repositoryName(REPOSITORY_NAME)
                .build();
        dummies.add(s2);
        Setting s3 = Setting.builder()
                .key("cron")
                .type(Setting.SettingsType.CRON)
                .value("true")
                .repositoryName(REPOSITORY_NAME)
                .build();
        dummies.add(s3);
        return dummies;
    }
}