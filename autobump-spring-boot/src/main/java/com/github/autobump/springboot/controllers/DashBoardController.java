package com.github.autobump.springboot.controllers;

import com.atlassian.connect.spring.IgnoreJwt;
import com.github.autobump.core.model.Repo;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryListDto;
import com.github.autobump.springboot.services.AutoBumpService;
import com.github.autobump.springboot.services.SettingsService;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Set;

@Controller
@Setter
@IgnoreJwt
public class DashBoardController {

    private SettingsService settingsService;
    private AutoBumpService autoBumpService;

    public DashBoardController(AutoBumpService autoBumpService, SettingsService settingsService) {
        this.autoBumpService = autoBumpService;
        this.settingsService = settingsService;
    }


    @GetMapping("/home")
    public ModelAndView home(ModelAndView mav) {
        List<RepositoryDto> monitored = settingsService.getMonitoredRepos();
        if (monitored.isEmpty()) {
            mav.setViewName("home");
            var repos = settingsService.getAllRepositories();
            mav.addObject("repositoryListDto", new RepositoryListDto(repos));
        } else {
            loadRepoOverview(mav);
        }
        return mav;
    }

    @PostMapping("/selectRepositories")
    public ModelAndView selectRepositories(ModelAndView mav, @ModelAttribute RepositoryListDto dto) {
        settingsService.updateSelectedFieldsOfRepos(dto);
        return loadRepoOverview(mav);
    }

    @GetMapping("/loadRepoOverview")
    public ModelAndView loadRepoOverview(ModelAndView mav) {
        mav.setViewName("settings");
        mav.addObject("repositories", settingsService.getMonitoredRepos());
        mav.addObject("rep", new RepositoryDto());
        return mav;
    }

    @GetMapping("/settings")
    public ModelAndView settings(ModelAndView mav, @RequestParam("repoId") String repoId) {
        mav.setViewName("repo-settings");
        Set<String> reviewers = settingsService.getReviewerNames(repoId);
        RepositoryDto dto = settingsService.getRepositoryDtoWithSettings(repoId);
        dto.setRepoId(repoId);
        if (dto.getReviewer() != null){
            mav.addObject("reviewerName", dto.getReviewer());
        }
        mav.addObject("repoName", dto.getName());
        mav.addObject("reviewerNames", reviewers);
        mav.addObject("repo", dto);
        return mav;
    }

    @GetMapping("/addRepos")
    public ModelAndView addRepos(ModelAndView mav) {
        mav.setViewName("home");
        var repos = settingsService.getAllRepositories();
        mav.addObject("repositoryListDto", new RepositoryListDto(repos));
        return mav;
    }

    @PostMapping("/selectRepos")
    public ModelAndView selectRepos(ModelAndView mav, RepositoryListDto dto) {
        mav.setViewName("settings");
        settingsService.updateSelectedFieldsOfRepos(dto);
        mav.addObject("repositories", settingsService.getMonitoredRepos());
        return mav;
    }

    @GetMapping("/bump")
    public ModelAndView bump(ModelAndView mav, @RequestParam("repoId") String repoId) {
        Repo repo = settingsService.getRepo(repoId);
        autoBumpService.executeAutoBump(repo.getLink());
        mav.setViewName("bumps");
        return mav;
    }

    @PostMapping("/saveSettings")
    public ModelAndView saveSettings(ModelAndView mav, RepositoryDto dto) {
        settingsService.saveSettings(dto);
        mav.setViewName("settings-saved");
        return mav;
    }
}
