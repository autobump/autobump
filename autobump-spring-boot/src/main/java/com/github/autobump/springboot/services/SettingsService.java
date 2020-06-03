package com.github.autobump.springboot.services;

import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.springboot.controllers.dtos.BranchDto;
import com.github.autobump.springboot.controllers.dtos.DependencyDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SettingsService {
    SettingsRepository settingsRepository;
    GitProvider gitProvider;

    // Needed: an object
    public List<RepositoryDto> getAllRepositories(){
        // rest call to bitbucket
        // check api response ...
        // check whether all repos still valid -> separate usecase?
        // branchlist refresh (add yes, delete no)
        // dependencies, check whether in settingsRepository
        // get settings for each repo from own database
        return createRepos();
    }

    private List<RepositoryDto> createRepos() {
        List<RepositoryDto> repos = new ArrayList<>();
        RepositoryDto repo1 = getOneRepositoryDto();
        repos.add(repo1);

        RepositoryDto repo2 = new RepositoryDto();
        repo2.setName("MultiModuleMavenProject");
        repo2.setId(2);
        repo2.setCronJob(true);
        repo2.setSelected(false);

        List<BranchDto> branches2 = new ArrayList<>();
        branches2.add(new BranchDto(4, "master"));
        branches2.add(new BranchDto(5, "development"));
        branches2.add(new BranchDto(6, "yetAnotherBranch"));

        repo2.setBranches(branches2);

        List<DependencyDto> dep2 = new ArrayList<>();
        dep2.add(new DependencyDto(3, "a group2", "an artifact2", "a version2", false, false));
        dep2.add(new DependencyDto(4, "another group2", "another artifact2", "another version2", false, false));

        repo2.setDependencies(dep2);
        repos.add(repo2);

        return repos;
    }

    public DependencyDto[] getAllDependenciesFromRepo() {
        List<DependencyDto> deps = new ArrayList<>();
        deps.add(new DependencyDto(1, "a group", "an artifact", "a version",false, false));
        deps.add(new DependencyDto(2, "another group", "another artifact", "another version", false, false));
        deps.add(new DependencyDto(3, "a group2", "an artifact2", "a version2", false, false));
        deps.add(new DependencyDto(4, "another group2", "another artifact2", "another version2", false, false));
        DependencyDto[] arrayList = new DependencyDto[deps.size()];
        return deps.toArray(arrayList);
    }

    public BranchDto[] getAllBranchesFromRepo(){
        List<BranchDto> branches = new ArrayList<>();
        branches.add(new BranchDto(1, "master"));
        branches.add(new BranchDto(2, "development"));
        branches.add(new BranchDto(3, "anotherBranch"));
        branches.add(new BranchDto(4, "master"));
        branches.add(new BranchDto(5, "development"));
        branches.add(new BranchDto(6, "yetAnotherBranch"));
        BranchDto[] arrayList = new BranchDto[branches.size()];
        return branches.toArray(arrayList);
    }

    public RepositoryDto getRepository(String repositoryName) {
        return getOneRepositoryDto();
    }

    private RepositoryDto getOneRepositoryDto() {
        RepositoryDto repo1 = new RepositoryDto();
        repo1.setName("TestMavenProject");
        repo1.setId(1);
        repo1.setSelected(false);

        List<BranchDto> branches1 = new ArrayList<>();
        branches1.add(new BranchDto(1, "master"));
        branches1.add(new BranchDto(2, "development"));
        branches1.add(new BranchDto(3, "anotherBranch"));

        repo1.setBranches(branches1);

        List<DependencyDto> dep1 = new ArrayList<>();
        dep1.add(new DependencyDto(1, "a group", "an artifact", "a version", false, false));
        dep1.add(new DependencyDto(2, "another group", "another artifact", "another version", false, false));

        repo1.setDependencies(dep1);
        return repo1;
    }
}
