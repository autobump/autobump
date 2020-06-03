package com.github.autobump.springboot.controllers;

import com.atlassian.connect.spring.IgnoreJwt;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryListDto;
import com.github.autobump.springboot.services.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SettingsController {
    @Autowired
    SettingsService settingsService;

    @IgnoreJwt
    @GetMapping("/settings")
    public ModelAndView settings(ModelAndView mav){
        var repos = settingsService.getAllRepositoriesFromWorkspace();
        mav.setViewName("settings");
        mav.addObject("repositoryListDto", new RepositoryListDto(repos));
        return mav;
    }

    @IgnoreJwt
    @PostMapping("/selectRepositories")
    public ModelAndView selectRepositories(ModelAndView mav, @ModelAttribute RepositoryListDto dto){
        mav.setViewName("select-repository");
        List<RepositoryDto> repos = dto.getRepositories()
                .stream()
                .filter(r -> r.isSelected() && r.isIgnore())
                .collect(Collectors.toList());
        // save settings in repo
        mav.addObject("repositories", repos);
        return mav;
    }

    @IgnoreJwt
    @GetMapping("/ignoreDependencies")
    public ModelAndView ignoreDependencies(ModelAndView mav,
                                           @RequestParam("repoName") String repoName){
        mav.setViewName("dependencies");
        mav.addObject("repositoryDto", settingsService.getRepository(repoName));
        return mav;
    }

    @IgnoreJwt
    @PostMapping("/saveIgnoredDependencies")
    public ModelAndView saveIgnoredDependencies(ModelAndView mav, RepositoryDto dto){
        // save ignore dependencies in settingsrepository
        // return to view to select other repo for configuring dependencies
        mav.setViewName("settings-saved");
        return mav;
    }

}
