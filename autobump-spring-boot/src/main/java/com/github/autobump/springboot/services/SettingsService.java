package com.github.autobump.springboot.services;

import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.springboot.controllers.dtos.DependencyDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SettingsService {
    SettingsRepository settingsRepository;

    // Needed: an object
    public List<RepositoryDto> getAllRepositoriesFromWorkspace(){
        // rest call to bitbucket
        // check whether all repos still valid -> separate usecase?
        // get settings for each repo from own database
        return createRepos();
    }

    private List<RepositoryDto> createRepos() {
        List<RepositoryDto> repos = new ArrayList<>();
        RepositoryDto repo1 = new RepositoryDto();
        repo1.setName("testMavenProject");
        repo1.setId(1);
        repo1.setSelected(false);

        List<DependencyDto> dep1 = new ArrayList<>();
        dep1.add(new DependencyDto("a group", "an artifact", "a version"));
        dep1.add(new DependencyDto("another group", "another artifact", "another version"));

        repo1.setDependencies(dep1);
        repos.add(repo1);

        RepositoryDto repo2 = new RepositoryDto();
        repo2.setName("multiModuleMavenProject");
        repo2.setId(2);
        repo2.setCronJob(true);
        repo2.setSelected(false);

        List<DependencyDto> dep2 = new ArrayList<>();
        dep2.add(new DependencyDto("a group2", "an artifact2", "a version2"));
        dep2.add(new DependencyDto("another group2", "another artifact2", "another version2"));

        repo2.setDependencies(dep2);
        repos.add(repo2);

        return repos;
    }

    public  void setRepositoryToAutobump(RepositoryDto dto){
        dto.setSelected(true);
    }

}
