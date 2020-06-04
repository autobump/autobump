package com.github.autobump.springboot.services;

import com.github.autobump.core.model.GitProvider;
import com.github.autobump.springboot.configuration.Autobumpconfig;
import com.github.autobump.springboot.controllers.dtos.DependencyDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import com.github.autobump.springboot.domain.Repo;
import com.github.autobump.springboot.repositories.RepoRepository;
import com.github.autobump.springboot.repositories.SpringSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
    @InjectMocks
    SettingsService service;

    List<Repo> dummyRepos = getDummyRepoList();


    @BeforeEach
    void setUp() {
        service = new SettingsService();
        service.setRepoRepository(repoRepository);
        service.setModelMapper(modelMapper);
        //when(autobumpconfig.getGitProvider()).thenReturn(gitProvider);
    }

    @Test
    void getAllRepositories() {
        // TODO - fix gitprovider first
    }

    @Test
    void getMonitoredRepos() {
        when(repoRepository.findAll()).thenReturn(dummyRepos);
        when(modelMapper.map(any(Repo.class), any())).thenReturn(new RepositoryDto());
        assertThat(service.getMonitoredRepos().size()).isEqualTo(1);
    }

    @Test
    void getSettingsForRepository() {
    }

    @Test
    void doAutoBump() {
    }

    @Test
    void seedDependencies() {
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

    private List<DependencyDto> getDummyDependencyDtos(){
        List<DependencyDto> deps = new ArrayList<>();
        deps.add(new DependencyDto("a group", "an artifact", "a version",false, false));
        deps.add(new DependencyDto( "another group", "another artifact", "another version", false, false));
        deps.add(new DependencyDto( "a group2", "an artifact2", "a version2", false, false));
        deps.add(new DependencyDto( "another group2", "another artifact2", "another version2", false, false));
        return deps;
    }
}