package com.github.autobump.springboot.services;

import com.atlassian.connect.spring.AtlassianHostRepository;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.Repo;
import com.github.autobump.core.model.RepoRepository;
import com.github.autobump.core.model.Setting;
import com.github.autobump.springboot.configuration.Autobumpconfig;
import com.github.autobump.springboot.controllers.dtos.DependencyDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import com.github.autobump.springboot.repositories.SpringSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
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
        setupService();
        dummyRepos = getDummyRepoList();
        setupDummyRepoDto();
    }

    private void setupDummyRepoDto() {
        dummyRepoDto = new RepositoryDto();
        List<DependencyDto> expectedDependencyList = getDependencyDtos();
        dummyRepoDto.setRepoId("a");
        dummyRepoDto.setSelected(true);
        dummyRepoDto.setName(REPOSITORY_NAME);
        dummyRepoDto.setCronJob(true);
        dummyRepoDto.setReviewer("name of a reviewer");
        dummyRepoDto.setDependencies(expectedDependencyList);
    }

    private List<DependencyDto> getDependencyDtos() {
        List<DependencyDto> expectedDependencyList = new ArrayList<>();
        expectedDependencyList.add(DependencyDto.builder()
                .groupName("org.projectlombok")
                .artifactId("lombok")
                .versionNumber("1.18.12")
                .ignoreMajor(true)
                .ignoreMinor(false)
                .build());
        return expectedDependencyList;
    }

    private void setupService() {
        service = new SettingsService(autobumpconfig);
        service.setRepoRepository(repoRepository);
        service.setModelMapper(modelMapper);
        service.setSettingsRepository(springSettingsRepository);
    }

    @Test
    void getAllRepositories_when_none_in_repo() {
        when(autobumpconfig.getGitProvider()).thenReturn(gitProvider);
        when(gitProvider.getRepos()).thenReturn(dummyRepos);
        when(repoRepository.findAll()).thenReturn(Collections.emptyList());
        when(modelMapper.map(any(Repo.class), any()))
                .thenReturn(new RepositoryDto());
        assertThat(service.getAllRepositories().size()).isEqualTo(2);
    }

    @Test
    void getAllRepositories_when_already_in_repo(){
        when(autobumpconfig.getGitProvider()).thenReturn(gitProvider);
        List<Repo> dummySaved = new ArrayList<>();
        dummySaved.add(getDummyRepo2());
        when(gitProvider.getRepos()).thenReturn(getDummyRepoList());
        when(repoRepository.findAll()).thenReturn(dummySaved);
        when(modelMapper.map(any(Repo.class), any()))
                .thenReturn(new RepositoryDto());
        assertThat(service.getAllRepositories().size()).isEqualTo(2);
    }

    @Test
    void getAllRepositories_when_remote_no_longer_exists(){
        when(autobumpconfig.getGitProvider()).thenReturn(gitProvider);
        List<Repo> dummyRemote = new ArrayList<>();
        dummyRemote.add(getDummyRepo1());
        when(gitProvider.getRepos()).thenReturn(dummyRemote);
        when(repoRepository.findAll()).thenReturn(getDummyRepoList());
        when(modelMapper.map(any(Repo.class), any()))
                .thenReturn(new RepositoryDto());
        assertThat(service.getAllRepositories().size()).isEqualTo(1);
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
        assertThat(service.getSettingsForRepository(REPOSITORY_NAME).getDependencies().size()).isEqualTo(1);
    }

    @Test
    void getSettingsOnlyReviewer(){
        List<Setting> settingsOnlyReviewer = new ArrayList<>();
        settingsOnlyReviewer.add(getReviewerSetting());
        when(springSettingsRepository.findAllSettingsForDependencies(REPOSITORY_NAME))
                .thenReturn(settingsOnlyReviewer);
        assertThat(service.getSettingsForRepository(REPOSITORY_NAME).getReviewer()).isNotNull();
    }

    @Test
    void getSettingsNoReviewer(){
        List<Setting> settingsNoReviewer = new ArrayList<>();
        settingsNoReviewer.add(getCronjobSetting());
        when(springSettingsRepository.findAllSettingsForDependencies(REPOSITORY_NAME))
                .thenReturn(settingsNoReviewer);
        assertThat(service.getSettingsForRepository(REPOSITORY_NAME).getReviewer()).isNotNull();
    }

    @Test
    void getSettingsNoCronJob(){
        List<Setting> settingsNoCronJob = new ArrayList<>();
        settingsNoCronJob.add(getReviewerSetting());
        when(springSettingsRepository.findAllSettingsForDependencies(REPOSITORY_NAME))
                .thenReturn(settingsNoCronJob);
        assertThat(service.getSettingsForRepository(REPOSITORY_NAME).getReviewer()).isNotNull();
    }

    @Test
    void getRepository() {
        when(repoRepository.getByRepoId(anyString())).thenReturn(dummyRepos.get(0));
        when(modelMapper.map(dummyRepos.get(0), RepositoryDto.class)).thenReturn(dummyRepoDto);
        assertThat(service.getRepository(anyString())).isEqualTo(dummyRepoDto);
    }

    @Test
    void updateRepo() {
        when(repoRepository.getByRepoId("a")).thenReturn(getDummyRepo1());
        assertThatCode(() -> service.updateRepo(dummyRepoDto)).doesNotThrowAnyException();
    }

    @Test
    void getRepo(){
        lenient().when(repoRepository.getByRepoId("a")).thenReturn(getDummyRepo1());
        assertThat(service.getRepo("a").getName()).isEqualTo(REPOSITORY_NAME);
    }

    @Test
    void saveSettings() {
        lenient().when(springSettingsRepository.findAllSettingsForDependencies(REPOSITORY_NAME))
                .thenReturn(getDummySettings());
        assertThatCode(() -> service.saveSettings(dummyRepoDto)).doesNotThrowAnyException();
    }

    @Test
    void saveSettings_without_reviewer(){
        RepositoryDto repositoryDtoWithoutReviewer = new RepositoryDto();
        assertThatCode(() -> service.saveSettings(repositoryDtoWithoutReviewer)).doesNotThrowAnyException();
    }


    @Test
    void saveSettings_removeCronJob(){
        lenient().when(springSettingsRepository.findAllSettingsForDependencies(REPOSITORY_NAME))
                .thenReturn(getDummySettings());
        dummyRepoDto.setCronJob(false);
        assertThatCode(() -> service.saveSettings(dummyRepoDto)).doesNotThrowAnyException();

    }

    private List<Repo> getDummyRepoList() {
        List<Repo> repos = new ArrayList<>();
        Repo repo = getDummyRepo1();
        repos.add(repo);
        Repo repo2 = getDummyRepo2();
        repos.add(repo2);
        return repos;
    }

    private Repo getDummyRepo1() {
        Repo repo2 = new Repo();
        repo2.setName("TestMavenProject");
        repo2.setSelected(true);
        repo2.setRepoId("a");
        return repo2;
    }

    private Repo getDummyRepo2() {
        Repo repo = new Repo();
        repo.setName("MultiModuleMavenProject");
        repo.setSelected(false);
        repo.setRepoId("b");
        return repo;
    }

    private List<Setting> getDummySettings(){
        List<Setting> dummies = new ArrayList<>();
        Setting s1 = getIgnoreSetting();
        dummies.add(s1);
        Setting s2 = getReviewerSetting();
        dummies.add(s2);
        Setting s3 = getCronjobSetting();
        dummies.add(s3);
        return dummies;
    }

    private Setting getCronjobSetting() {
        return Setting.builder()
                    .key("cron")
                    .type(Setting.SettingsType.CRON)
                    .value("true")
                    .repositoryName(REPOSITORY_NAME)
                    .build();
    }

    private Setting getReviewerSetting() {
        return Setting.builder()
                    .key("reviewer")
                    .type(Setting.SettingsType.REVIEWER)
                    .value("name of a reviewer")
                    .repositoryName(REPOSITORY_NAME)
                    .build();
    }

    private Setting getIgnoreSetting() {
        return Setting.builder()
                    .repositoryName(REPOSITORY_NAME)
                    .key("org.projectlombok:lombok:1.18.12")
                    .type(Setting.SettingsType.IGNORE)
                    .value("major")
                    .build();
    }


}