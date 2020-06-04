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

@Controller
public class SettingsController {
    @Autowired
    SettingsService settingsService;

    @IgnoreJwt
    @GetMapping("/home")
    public ModelAndView home(ModelAndView mav) {
        List<RepositoryDto> monitored = settingsService.getMonitoredRepos();
        if (monitored.isEmpty()) {
            mav.setViewName("home");
            var repos = settingsService.getAllRepositories();
            mav.addObject("repositoryListDto", new RepositoryListDto(repos));
        } else {
            mav.setViewName("settings");
            loadRepoOverview(mav);
        }
        return mav;
    }

    @IgnoreJwt
    @PostMapping("/selectRepositories")
    public ModelAndView selectRepositories(ModelAndView mav, @ModelAttribute RepositoryListDto dto) {
        updateSelectedFieldsOfRepos(dto);
        ModelAndView modelAndView = loadRepoOverview(mav);
        return modelAndView;
    }

    @IgnoreJwt
    @GetMapping("/loadRepoOverview")
    private ModelAndView loadRepoOverview(ModelAndView mav) {
        mav.setViewName("settings");
        mav.addObject("repositories", settingsService.getMonitoredRepos());
        mav.addObject("rep", new RepositoryDto());
        return mav;
    }

    @IgnoreJwt
    @GetMapping("/settings")
    public ModelAndView settings(ModelAndView mav, @RequestParam("repoId") int repoId) {
        mav.setViewName("repo-settings");
        String repoName = settingsService.getRepository(repoId).getName();
        RepositoryDto dto = settingsService.getSettingsForRepository(repoName);
        // TODO - to remove
        //dto.setDependencies(settingsService.seedDependencies());
        mav.addObject("repoName", repoName);
        mav.addObject("repo", dto);
        return mav;
    }

    @IgnoreJwt
    @GetMapping("/addRepos")
    public ModelAndView addRepos(ModelAndView mav) {
        mav.setViewName("home");
        var repos = settingsService.getAllRepositories();
        mav.addObject("repositoryListDto", new RepositoryListDto(repos));
        return mav;
    }

    @IgnoreJwt
    @PostMapping("/selectRepos")
    public ModelAndView selectRepos(ModelAndView mav, RepositoryListDto dto) {
        mav.setViewName("settings");
        updateSelectedFieldsOfRepos(dto);
        mav.addObject("repositories", settingsService.getMonitoredRepos());
        return mav;
    }

    @IgnoreJwt
    @GetMapping("/bump")
    public ModelAndView bump(ModelAndView mav, @RequestParam("repoId") int repoId) {
        settingsService.doAutoBump(repoId);
        mav.setViewName("bumps");
        return mav;
    }

    private void updateSelectedFieldsOfRepos(@ModelAttribute RepositoryListDto dto) {
        for (RepositoryDto repo: dto.getRepositories()
             ) {
            settingsService.updateRepo(repo);
        }
    }

    @IgnoreJwt
    @PostMapping("/saveSettings")
    public ModelAndView saveIgnoredDependencies(ModelAndView mav, RepositoryDto dto) {
        settingsService.saveSettings(dto);
        mav.setViewName("settings-saved");
        return mav;
    }
}
