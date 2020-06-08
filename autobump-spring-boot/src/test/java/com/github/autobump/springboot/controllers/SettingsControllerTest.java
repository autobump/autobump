package com.github.autobump.springboot.controllers;

import com.github.autobump.core.model.Repo;
import com.github.autobump.springboot.controllers.dtos.DependencyDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryListDto;
import com.github.autobump.springboot.services.AutoBumpService;
import com.github.autobump.springboot.services.SettingsService;
import com.github.tomakehurst.wiremock.WireMockServer;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SettingsControllerTest {
    public static final String REPOSITORY_NAME = "TestMavenProject";
    public static final String SETTINGS_VIEW = "settings";
    public static final String HOME_VIEW = "home";
    public static final String MOCK_REPO_ID = "{1b237a32-ca30-4182-8fe4-e90c73b61a13}";
    public static final String MOCK_REPO_ID_2 = "{8932aece-54e5-4186-ae0c-205470678da6}";
    private RepositoryDto dummyRepoDto;

    @Mock
    private SettingsService service;

    @Mock
    private AutoBumpService autoBumpService;

    @Autowired
    @InjectMocks
    private SettingsController settingsController;

    @BeforeEach
    void setUp() {
        settingsController.setSettingsService(service);
        settingsController.setAutoBumpService(autoBumpService);
        setupDummies();
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
        when(service.getRepositoryDtoWithSettings(anyString())).thenReturn(dummyRepoDto);
        ModelAndView mav = settingsController.settings(new ModelAndView(), MOCK_REPO_ID);
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
        WireMockServer wireMockServer = new WireMockServer(8009);
        wireMockServer.start();
        wireMockServer.stubFor(post(urlEqualTo("/site/oauth2/access_token"))
                .willReturn(aResponse().withBody("{\"access_token\": \"testtoken\"}")
                        .withHeader("content-type", "application/json")
                        .withStatus(200)));
        lenient().when(service.getRepo(MOCK_REPO_ID)).thenReturn(new Repo(MOCK_REPO_ID, "a_link", "a_name"));
        ModelAndView mav = settingsController.bump(new ModelAndView(), MOCK_REPO_ID);
        assertThat(mav.getViewName()).isEqualTo("bumps");
        wireMockServer.stop();
    }

    @Test
    void saveSettings() {
        ModelAndView mav = settingsController.saveSettings(new ModelAndView(), dummyRepoDto);
        assertThat(mav.getViewName()).isEqualTo("settings-saved");
    }

    private void setupDummies() {
        List<DependencyDto> expectedDependencyList = new ArrayList<>();
        expectedDependencyList.add(DependencyDto.builder()
                .groupName("org.projectlombok")
                .artifactId("lombok")
                .versionNumber("1.18.12")
                .ignoreMajor(true)
                .ignoreMinor(false)
                .build());
        dummyRepoDto = new RepositoryDto();
        dummyRepoDto.setRepoId(MOCK_REPO_ID);
        dummyRepoDto.setName(REPOSITORY_NAME);
        dummyRepoDto.setCronJob(true);
        dummyRepoDto.setReviewer("name of a reviewer");
        dummyRepoDto.setDependencies(expectedDependencyList);
    }

    private List<RepositoryDto> getDummyRepoList() {
        List<RepositoryDto> repos = new ArrayList<>();
        RepositoryDto repo = getRepositoryDto1();
        repos.add(repo);
        RepositoryDto repo2 = new RepositoryDto();
        getRepositoryDto2(repos, repo2);
        return repos;
    }

    private void getRepositoryDto2(List<RepositoryDto> repos, RepositoryDto repo2) {
        repo2.setName("TestMavenProject");
        repo2.setSelected(false);
        repo2.setRepoId(MOCK_REPO_ID_2);
        repos.add(repo2);
    }

    private RepositoryDto getRepositoryDto1() {
        RepositoryDto repo = new RepositoryDto();
        repo.setName("MultiModuleMavenProject");
        repo.setSelected(true);
        repo.setRepoId(MOCK_REPO_ID);
        return repo;
    }
}