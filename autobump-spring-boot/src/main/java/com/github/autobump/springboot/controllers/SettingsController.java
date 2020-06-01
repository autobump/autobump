package com.github.autobump.springboot.controllers;

import com.atlassian.connect.spring.IgnoreJwt;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryListDto;
import com.github.autobump.springboot.controllers.dtos.RepositorySettingsDto;
import com.github.autobump.springboot.controllers.dtos.SelectionDto;
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
    @GetMapping("/select")
    public ModelAndView index (ModelAndView mav){
        mav.setViewName("index");
        mav.addObject("selectionDto", new SelectionDto(true));
        return mav;
    }

    @IgnoreJwt
    @PostMapping("/selection")
    public ModelAndView selection(ModelAndView mav, @ModelAttribute SelectionDto selectionDto){
        var repos = settingsService.getAllRepositoriesFromWorkspace();
        if (selectionDto.isAll()){
            mav.setViewName("repositories");
            mav.addObject("repositories", repos);
        }
        else {
            mav.setViewName("select-repositories");
            mav.addObject("repositories", repos);
            RepositoryListDto repositoryListDto = new RepositoryListDto();
            repositoryListDto.setRepositories(repos);
            mav.addObject("repositoryListDto", repositoryListDto);
        }
        return mav;
    }

    @IgnoreJwt
    @PostMapping("/repositories")
    public ModelAndView repositories(ModelAndView mav, @ModelAttribute RepositoryListDto dto){
        mav.setViewName("repositories");
        for (RepositoryDto repo: dto.getRepositories()
             ) {
            settingsService.setRepositoryToAutobump(repo);
        }
        mav.addObject("repositories", dto);
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
