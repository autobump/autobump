package com.github.autobump.springboot.services;

import com.atlassian.connect.spring.AtlassianHostRepository;
import com.github.autobump.core.model.domain.Repo;
import com.github.autobump.core.model.domain.Setting;
import com.github.autobump.core.model.gitproviders.GitProvider;
import com.github.autobump.core.repositories.RepoRepository;
import com.github.autobump.springboot.configuration.Autobumpconfig;
import com.github.autobump.springboot.controllers.dtos.DependencyDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryListDto;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        dummyRepoDto.setName(REPOSITORY_NAME);
        dummyRepoDto.setCronJob(true);
        dummyRepoDto.setRepoId("a");
        dummyRepoDto.setReviewer("reviewer_name");
        dummyRepoDto.setDependencies(expectedDependencyList);
    }

    private List<DependencyDto> getDependencyDtos() {
        List<DependencyDto> expectedDependencyList = new ArrayList<>();
        expectedDependencyList.add(DependencyDto.builder().groupName("org.projectlombok")
                .artifactId("lombok").versionNumber("1.18.12").ignoreMajor(true).ignoreMinor(false).build());
        expectedDependencyList.add(DependencyDto.builder().groupName("com.google.code.gson")
                .artifactId("gson").versionNumber("2.2.2").ignoreMinor(true).ignoreMajor(false).build());
        return expectedDependencyList;
    }

    private void setupService() {
        service = new SettingsService(autobumpconfig, repoRepository,
                springSettingsRepository, modelMapper);
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
    void getContributorNames(){
        when(autobumpconfig.getGitProvider()).thenReturn(gitProvider);
        lenient().when(repoRepository.getByRepoId("a")).thenReturn(getDummyRepo1());
        Map<String, String> dummyMembers = new HashMap<>();
        dummyMembers.put("reviewer_name1", "reviewer_uuid1");
        dummyMembers.put("reviewer_owner_name", "reviewer_owner_uuid");
        lenient().when(gitProvider.getMembersFromWorkspace(getDummyRepo1())).thenReturn(dummyMembers);
        lenient().when(gitProvider.getCurrentUserUuid()).thenReturn("reviewer_owner_uuid");
        assertThat(service.getContributerNamesFromWorkspace(getDummyRepo1().getRepoId())).contains("reviewer_name1");
    }

    @Test
    void getSettingsForRepository() {
        when(autobumpconfig.getGitProvider()).thenReturn(gitProvider);
        when(springSettingsRepository.findAllSettingsForDependencies(REPOSITORY_NAME))
                .thenReturn(getDummySettings());
        Map<String, String> members = new HashMap<>();
        members.put("reviewer_name", "reviewer_uuid");
        when(gitProvider.getMembersFromWorkspace(any())).thenReturn(members);
        assertThat(service.getSettingsForRepository(REPOSITORY_NAME, "a").getDependencies().size()).isEqualTo(2);
    }

    @Test
    void getSettingsNoReviewer(){
        when(autobumpconfig.getGitProvider()).thenReturn(gitProvider);
        when(gitProvider.getMembersFromWorkspace(any())).thenReturn(Collections.emptyMap());
        List<Setting> settingsNoReviewer = new ArrayList<>();
        settingsNoReviewer.add(getCronjobSetting());
        when(springSettingsRepository.findAllSettingsForDependencies(REPOSITORY_NAME))
                .thenReturn(settingsNoReviewer);
        assertThat(service.getSettingsForRepository(REPOSITORY_NAME, "a").getReviewer()).isNotNull();
    }

    @Test
    void getSettingsNoCronJob(){
        when(autobumpconfig.getGitProvider()).thenReturn(gitProvider);
        List<Setting> settingsNoCronJob = new ArrayList<>();
        settingsNoCronJob.add(getReviewerSetting());
        when(springSettingsRepository.findAllSettingsForDependencies(REPOSITORY_NAME))
                .thenReturn(settingsNoCronJob);
        assertThat(service.getSettingsForRepository(REPOSITORY_NAME, "a").getReviewer()).isNotNull();
    }

    @Test
    void getRepository() {
        when(repoRepository.getByRepoId(anyString())).thenReturn(dummyRepos.get(0));
        when(modelMapper.map(dummyRepos.get(0), RepositoryDto.class)).thenReturn(dummyRepoDto);
        assertThat(service.getRepository(anyString())).isEqualTo(dummyRepoDto);
    }

    @Test
    void updateRepo() {
        dummyRepoDto.setRepoId("a");
        dummyRepoDto.setSelected(true);
        lenient().when(repoRepository.getByRepoId("a")).thenReturn(getDummyRepo1());
        assertThatCode(() -> service.updateRepo(dummyRepoDto)).doesNotThrowAnyException();
    }

    @Test
    void getRepo(){
        lenient().when(repoRepository.getByRepoId("a")).thenReturn(getDummyRepo1());
        assertThat(service.getRepo("a").getName()).isEqualTo(REPOSITORY_NAME);
    }

    @Test
    void saveSettings() {
        mockGitProviderReturnMembers();
        lenient().when(springSettingsRepository.findAllSettingsForDependencies(REPOSITORY_NAME))
                .thenReturn(getDummySettings());
        assertThatCode(() -> service.saveSettings(dummyRepoDto)).doesNotThrowAnyException();
    }

    @Test
    void saveSettings_without_reviewer(){
        RepositoryDto repositoryDtoWithoutReviewer = new RepositoryDto();
        repositoryDtoWithoutReviewer.setRepoId("c");
        repositoryDtoWithoutReviewer.setName(REPOSITORY_NAME);
        repositoryDtoWithoutReviewer.setReviewer("none");
        assertThatCode(() -> service.saveSettings(repositoryDtoWithoutReviewer)).doesNotThrowAnyException();
    }

    @Test
    void saveSettings_removeCronJob(){
        when(autobumpconfig.getGitProvider()).thenReturn(gitProvider);
        lenient().when(springSettingsRepository.findAllSettingsForDependencies(REPOSITORY_NAME))
                .thenReturn(getDummySettings());
        dummyRepoDto.setCronJob(false);
        assertThatCode(() -> service.saveSettings(dummyRepoDto)).doesNotThrowAnyException();
    }

    @Test
    void getRepositoryDtoWithSettings(){
        mockGitProviderReturnMembers();
        RepositoryDto dummyRepoWithSettings = new RepositoryDto();
        dummyRepoWithSettings.setDependencies(getDependencyDtos());
        lenient().when(springSettingsRepository.findAllSettingsForDependencies(anyString()))
                .thenReturn(getDummySettings());
        lenient().when(repoRepository.getByRepoId(anyString())).thenReturn(getDummyRepo1());
        assertThat(service.getRepositoryDtoWithSettings("a")).isEqualToComparingFieldByField(dummyRepoDto);
    }

    private void mockGitProviderReturnMembers() {
        when(autobumpconfig.getGitProvider()).thenReturn(gitProvider);
        Map<String, String> members = new HashMap<>();
        members.put("reviewer_name", "reviewer_uuid");
        when(gitProvider.getMembersFromWorkspace(any())).thenReturn(members);
    }

    @Test
    void updateSelectedFieldsOfRepos(){
        dummyRepoDto.setRepoId("a");
        dummyRepoDto.setSelected(true);
        List<RepositoryDto> dtos = new ArrayList<>();
        dtos.add(dummyRepoDto);
        RepositoryListDto dto = new RepositoryListDto(dtos);
        lenient().when(repoRepository.getByRepoId(anyString())).thenReturn(getDummyRepo1());
        assertThatCode(() -> service.updateSelectedFieldsOfRepos(dto)).doesNotThrowAnyException();
    }

    private List<Repo> getDummyRepoList() {
        return List.of(getDummyRepo1(), getDummyRepo2());
    }

    private Repo getDummyRepo1() {
        Repo repo2 = new Repo();
        repo2.setName(REPOSITORY_NAME);
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
        return List.of(getIgnoreSetting1(), getIgnoreSetting2(), getIgnoreSetting3(),
                getCronjobSetting(), getReviewerSetting());
    }

    private Setting getCronjobSetting() {
        return Setting.builder().key("cron").type(Setting.SettingsType.CRON).value("true")
                .repositoryName(REPOSITORY_NAME).build();
    }

    private Setting getReviewerSetting() {
        return Setting.builder().key("reviewer").type(Setting.SettingsType.REVIEWER).value("reviewer_uuid")
                .repositoryName(REPOSITORY_NAME).build();
    }

    private Setting getIgnoreSetting1() {
        return Setting.builder().repositoryName(REPOSITORY_NAME).key("org.projectlombok:lombok:1.18.12")
                    .type(Setting.SettingsType.IGNORE).value("Major").build();
    }

    private Setting getIgnoreSetting2() {
        return Setting.builder().repositoryName(REPOSITORY_NAME).key("junit:junit").type(Setting.SettingsType.IGNORE)
                .value("3.8.1").build();
    }

    private Setting getIgnoreSetting3() {
        return Setting.builder().repositoryName(REPOSITORY_NAME).key("com.google.code.gson:gson:2.2.2")
                .type(Setting.SettingsType.IGNORE).value("Minor").build();
    }
}
