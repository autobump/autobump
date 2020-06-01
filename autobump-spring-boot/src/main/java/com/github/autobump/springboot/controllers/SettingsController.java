package com.github.autobump.springboot.controllers;

import com.atlassian.connect.spring.IgnoreJwt;
import com.github.autobump.springboot.controllers.dtos.RepositorySettingsDto;
import com.github.autobump.springboot.controllers.dtos.SelectionDto;
import com.github.autobump.springboot.services.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        return mav;
    }

    @IgnoreJwt
    @PostMapping("/selection")
    public ModelAndView selection(ModelAndView mav, SelectionDto dto){
        var repos = settingsService.getAllRepositoriesFromWorkspace();
        if (dto.isAll()){
            mav.setViewName("repositories");
            mav.addObject("repositories", repos);
        }
        else {
            mav.setViewName("select-repositories");
            mav.addObject("repositories", repos);
        }
        return mav;
    }

    @IgnoreJwt
    @PostMapping("/repositories")
    public ModelAndView repositories(ModelAndView mav, @RequestParam(value="repos", required = false) String[] repos){
        mav.setViewName("repositories");
        settingsService.setRepositoriesToAutobump(repos);
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
