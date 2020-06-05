package com.github.autobump.springboot.controllers;

import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
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

    @Mock
    SettingsService service;

    @Autowired
    @InjectMocks
    SettingsController settingsController;

    @BeforeEach
    void setUp() {
        settingsController.setSettingsService(service);
    }

    @Test
    void home_with_monitored_repos() {
        when(service.getMonitoredRepos()).thenReturn(getDummyRepoList());
        ModelAndView mav = settingsController.home(new ModelAndView());
        assertThat("settings").isEqualTo(mav.getViewName());
    }

    @Test
    void home_without_monitored_repos(){
        when(service.getMonitoredRepos()).thenReturn(new ArrayList<>());
        ModelAndView mav = settingsController.home(new ModelAndView());
        assertThat("home").isEqualTo(mav.getViewName());
    }

    @Test
    void selectRepositories() {

    }

    @Test
    void settings() {
    }

    @Test
    void addRepos() {
    }

    @Test
    void selectRepos() {
    }

    @Test
    void bump() {
    }

    @Test
    void saveIgnoredDependencies() {
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