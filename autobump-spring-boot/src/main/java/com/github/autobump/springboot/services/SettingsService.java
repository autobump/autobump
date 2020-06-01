package com.github.autobump.springboot.services;

import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SettingsService {
    SettingsRepository settingsRepository;

    // Needed: an object
    public List<RepositoryDto> getAllRepositoriesFromWorkspace(){
        //settingsRepository.getAllReponames();
        RepositoryDto repo1 = new RepositoryDto("repo1", 1);
        RepositoryDto repo2 = new RepositoryDto("repo2", 2);
        return List.of(repo1, repo2);
    }

    public  void setRepositoriesToAutobump(String[] repos){
        //save reponames in the settingsrepository
    }

}
