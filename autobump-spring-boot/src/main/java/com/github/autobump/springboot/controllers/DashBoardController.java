package com.github.autobump.springboot.controllers;

import com.atlassian.connect.spring.IgnoreJwt;
import com.github.autobump.bitbucket.exceptions.BitbucketUnauthorizedException;
import com.github.autobump.core.model.domain.Repo;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryListDto;
import com.github.autobump.springboot.services.AutoBumpService;
import com.github.autobump.springboot.services.SettingsService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@Setter
@IgnoreJwt
public class DashBoardController {

    private SettingsService settingsService;
    private AutoBumpService autoBumpService;
    @Value("${autobump.bitbucket.base-url}")
    private String baseUrl;

    public DashBoardController(AutoBumpService autoBumpService, SettingsService settingsService) {
        this.autoBumpService = autoBumpService;
        this.settingsService = settingsService;
    }

    @GetMapping("/")
    public ModelAndView bitbucket(ModelAndView mav){
        mav.addObject("baseUrl",baseUrl);
        mav.setViewName("bitbucket");
        return mav;
    }

    @GetMapping("/home")
    public ModelAndView home(ModelAndView mav) {
        try{
            List<RepositoryDto> monitored = settingsService.getMonitoredRepos();
            if (monitored.isEmpty()) {
                mav.setViewName("home");
                mav.addObject("repositoryListDto",
                        new RepositoryListDto(settingsService.getAllRepositories()));
            } else {
                loadRepoOverview(mav);
            }
        }
        catch(BitbucketUnauthorizedException b){
            mav.addObject("baseUrl",baseUrl);
            mav.setViewName("bitbucket");
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
        RepositoryDto dto = settingsService.getRepositoryDtoWithSettings(repoId);
        dto.setRepoId(repoId);
        if (dto.getReviewer() != null){
            mav.addObject("reviewerName", dto.getReviewer());
        }
        mav.addObject("repoName", dto.getName());
        mav.addObject("reviewerNames", settingsService.getContributerNamesFromWorkspace(repoId));
        mav.addObject("repo", dto);
        return mav;
    }

    @GetMapping("/addRepos")
    public ModelAndView addRepos(ModelAndView mav) {
        try{
            mav.setViewName("home");
            var repos = settingsService.getAllRepositories();
            mav.addObject("repositoryListDto", new RepositoryListDto(repos));
        }catch (BitbucketUnauthorizedException b) {
            mav.addObject("baseUrl", baseUrl);
            mav.setViewName("bitbucket");
        }
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
