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
        RepositoryDto repo1 = new RepositoryDto(false, "repo1", 1);
        RepositoryDto repo2 = new RepositoryDto(false,"repo2", 2);
        return List.of(repo1, repo2);
    }

    public  void setRepositoryToAutobump(RepositoryDto dto){
        dto.setSelected(true);
    }

}
