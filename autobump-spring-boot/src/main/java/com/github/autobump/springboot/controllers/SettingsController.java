package com.github.autobump.springboot.controllers;

import com.atlassian.connect.spring.IgnoreJwt;
import com.github.autobump.springboot.controllers.dtos.RepositoryListDto;
import com.github.autobump.springboot.controllers.dtos.RepositorySettingsDto;
import com.github.autobump.springboot.services.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class SettingsController {
    @Autowired
    SettingsService settingsService;

    @IgnoreJwt
    @GetMapping("/settings")
    public ModelAndView settings(ModelAndView mav){
        var repos = settingsService.getAllRepositoriesFromWorkspace();
        mav.setViewName("settings");
        mav.addObject("repositories", repos);
        RepositoryListDto repositoryListDto = new RepositoryListDto();
        repositoryListDto.setRepositories(repos);
        mav.addObject("repositoryListDto", repositoryListDto);
        return mav;
    }

    @IgnoreJwt
    @PostMapping("/saveSettings")
    public ModelAndView saveSettings(ModelAndView mav, @ModelAttribute RepositoryListDto dto){
        mav.setViewName("settings-saved");
        /*for (RepositoryDto repo: dto.getRepositories()
             ) {
            settingsService.setRepositoryToAutobump(repo);
        }
        mav.addObject("repositories", dto);*/
        return mav;
    }

    @IgnoreJwt
    @PostMapping("/settings")
    public ModelAndView addSettingsToRepositories(ModelAndView mav, List<RepositorySettingsDto> settings){
        // save all settings to settingsrepository
        mav.setViewName("settings-saved");
        return mav;
    }

}
