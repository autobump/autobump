package com.github.autobump.springboot.controllers;

import com.github.autobump.springboot.controllers.dtos.RepositorySettingsDto;
import com.github.autobump.springboot.controllers.dtos.SelectionDto;
import com.github.autobump.springboot.services.SettingsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class SettingsController {
    SettingsService settingsService;

    @GetMapping("/")
    public ModelAndView index (ModelAndView mav){
        mav.setViewName("index");
        return mav;
    }

    @PostMapping("/selection")
    public ModelAndView selection(ModelAndView mav, SelectionDto dto){
        if (dto.isAll()){
            mav.setViewName("repositories");
            var repos = settingsService.getAllRepositoriesFromWorkspace();
            mav.addObject("repositories", repos);
        }
        else {
            mav.setViewName("select-repositories");
            mav.addObject("repositories", settingsService.getAllRepositoriesFromWorkspace());
        }
        return mav;
    }

    @PostMapping("/repositories")
    public ModelAndView repositories(ModelAndView mav, @RequestParam(value="repos", required = false) String[] repos){
        mav.setViewName("repositories");
        settingsService.setRepositoriesToAutobump(repos);
        return mav;
    }

    @PostMapping("/settings")
    public ModelAndView addSettingsToRepositories(ModelAndView mav, List<RepositorySettingsDto> settings){
        // save all settings to settingsrepository
        mav.setViewName("settings-saved");
        return mav;
    }

}
