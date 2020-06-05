package com.github.autobump.springboot.controllers;

import com.github.autobump.springboot.controllers.dtos.DependencyDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryListDto;
import com.github.autobump.springboot.services.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SettingsControllerTest {
    public static final String REPOSITORY_NAME = "TestMavenProject";
    public static final String SETTINGS_VIEW = "settings";
    public static final String HOME_VIEW = "home";
    private RepositoryDto dummyRepoDto;

    @Mock
    SettingsService service;

    @Autowired
    @InjectMocks
    SettingsController settingsController;

    @BeforeEach
    void setUp() {
        settingsController.setSettingsService(service);
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
    void home_with_monitored_repos() {
        when(service.getMonitoredRepos()).thenReturn(getDummyRepoList());
        ModelAndView mav = settingsController.home(new ModelAndView());
        assertThat(mav.getViewName()).isEqualTo(SETTINGS_VIEW);
    }

    @Test
    void home_without_monitored_repos(){
        when(service.getMonitoredRepos()).thenReturn(new ArrayList<>());
        ModelAndView mav = settingsController.home(new ModelAndView());
        assertThat(mav.getViewName()).isEqualTo(HOME_VIEW);
    }

    @Test
    void selectRepositories() {
        when(service.getMonitoredRepos()).thenReturn(getDummyRepoList());
        ModelAndView mav = settingsController
                .selectRepositories(new ModelAndView(), new RepositoryListDto(getDummyRepoList()));
        assertThat(mav.getViewName()).isEqualTo(SETTINGS_VIEW);
    }

    @Test
    void settings() {
        when(service.getRepository(1)).thenReturn(dummyRepoDto);
        when(service.getSettingsForRepository(REPOSITORY_NAME)).thenReturn(dummyRepoDto);
        ModelAndView mav = settingsController.settings(new ModelAndView(), 1);
        assertThat(mav.getModel().get("repoName")).isEqualTo(REPOSITORY_NAME);
    }

    @Test
    void addRepos() {
        when(service.getAllRepositories()).thenReturn(getDummyRepoList());
        ModelAndView mav = settingsController.addRepos(new ModelAndView());
        assertThat(mav.getViewName()).isEqualTo(HOME_VIEW);

    }

    @Test
    void selectRepos() {
        ModelAndView mav = settingsController
                .selectRepos(new ModelAndView(), new RepositoryListDto(getDummyRepoList()));
        assertThat(mav.getViewName()).isEqualTo(SETTINGS_VIEW);
    }

    @Test
    void bump() {
        ModelAndView mav = settingsController.bump(new ModelAndView(), 1);
        assertThat(mav.getViewName()).isEqualTo("bumps");
    }

    @Test
    void saveSettings() {
        ModelAndView mav = settingsController.saveSettings(new ModelAndView(), dummyRepoDto);
        assertThat(mav.getViewName()).isEqualTo("settings-saved");
    }

    private List<RepositoryDto> getDummyRepoList() {
        List<RepositoryDto> repos = new ArrayList<>();
        RepositoryDto repo = new RepositoryDto();
        repo.setName("MultiModuleMavenProject");
        repo.setSelected(true);
        repo.setRepoId(1);
        repos.add(repo);
        RepositoryDto repo2 = new RepositoryDto();
        repo2.setName("TestMavenProject");
        repo2.setSelected(false);
        repo2.setRepoId(2);
        repos.add(repo2);
        return repos;
    }
}