package com.github.autobump.springboot.services;

import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.usecases.AutobumpUseCase;
import com.github.autobump.github.model.GithubReleaseNotesSource;
import com.github.autobump.springboot.configuration.Autobumpconfig;
import com.github.autobump.springboot.controllers.dtos.DependencyDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import com.github.autobump.springboot.domain.Repo;
import com.github.autobump.springboot.repositories.RepoRepository;
import com.github.autobump.springboot.repositories.SpringSettingsRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SettingsService {
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    SpringSettingsRepository settingsRepository;
    @Autowired
    RepoRepository repoRepository;
    @Autowired
    Autobumpconfig autobumpconfig;

    public List<RepositoryDto> getAllRepositories(){
        GitProvider gitProvider = autobumpconfig.getGitProvider();
        List<Repo> remoteRepos = seed(); //TODO gitprovider.getRepositories
        List<Repo> savedRepos = repoRepository.findAll();

        addNewRemoteRepos(remoteRepos, savedRepos);
        removeReposNoLongerRemotelyPresent(remoteRepos, savedRepos);

        return savedRepos
                .stream()
                .map(r -> modelMapper.map(r, RepositoryDto.class))
                .collect(Collectors.toList());
    }

    private void addNewRemoteRepos(List<Repo> remoteRepos, List<Repo> savedRepos) {
        for (Repo repo : remoteRepos
             ) {
            Optional<Repo> opt = savedRepos.stream().filter(s -> s.getRepoId() == repo.getRepoId()).findAny();
            if (opt.isEmpty()){
                savedRepos.add(repo);
                repoRepository.save(repo);
            }
        }
    }

    private void removeReposNoLongerRemotelyPresent(List<Repo> remoteRepos, List<Repo> savedRepos) {
        for (Repo repo : savedRepos
        ) {
            Optional<Repo> opt = remoteRepos.stream().filter(r -> r.getRepoId() == repo.getRepoId()).findAny();
            if (opt.isEmpty()) {
                savedRepos.remove(repo);
                repoRepository.delete(repo);
            }
        }
    }

    public List<RepositoryDto> getMonitoredRepos() {
        return repoRepository.findAll()
                .stream()
                .filter(Repo::isSelected)
                .map(repo -> modelMapper.map(repo, RepositoryDto.class))
                .collect(Collectors.toUnmodifiableList());
    }

    public RepositoryDto getSettingsForRepository(String repoName){
        List<Setting> settings = settingsRepository.findAllSettingsForDependencies(repoName);
        RepositoryDto dto = new RepositoryDto();
        String reviewer = getReviewerFromSetting(settings);
        boolean cronJob = getIsCronJob(settings);
        List<DependencyDto> dependencyDtos = getIgnoredDependenciesFromSettings(settings);
        dto.setReviewer(reviewer);
        dto.setCronJob(cronJob);
        dto.setDependencies(dependencyDtos);
        dto.setName(repoName);
        return dto;
    }

    private String getReviewerFromSetting(List<Setting> settings) {
        String rev = "";
        Setting reviewer = settings
                .stream()
                .filter(s -> s.getType().equals(Setting.SettingsType.REVIEWER))
                .findFirst().orElse(null);
        if (reviewer != null) rev = reviewer.getValue();
        return rev;
    }

    private boolean getIsCronJob(List<Setting> settings) {
        Setting isCronJob = settings
                .stream()
                .filter(s -> s.getType().equals(Setting.SettingsType.CRON))
                .findFirst().orElse(null);
        return isCronJob != null;
    }

    private List<DependencyDto> getIgnoredDependenciesFromSettings(List<Setting> settings) {
        List<DependencyDto> dtos = new ArrayList<>();
        List<Setting> depSettings = settings
                .stream()
                .filter(s -> s.getType().equals(Setting.SettingsType.IGNORE))
                .collect(Collectors.toList());
        for (Setting setting: depSettings
             ) {
            DependencyDto dep = new DependencyDto();
            dep.setGav(setting.getKey());
            dep.setIgnoreMajor(setting.getValue().equals("major"));
            dep.setIgnoreMinor(setting.getValue().equals("minor"));
            dtos.add(dep);
        }
        return dtos;
    }

    public void doAutoBump(int repoId){
        AutobumpUseCase.builder()
                .config(autobumpconfig.setupConfig())
                .releaseNotesSource(new GithubReleaseNotesSource("https://api.github.com"))
                .uri(URI.create(repoRepository.getByRepoId(repoId).getLink()))
                .build()
                .doAutoBump();
    }

    // TODO - should be removed when gitprovider used
    private List<Repo> seed() {
        List<Repo> repos = new ArrayList<>();
        Repo repo = new Repo();
        repo.setName("MultiModuleMavenProject");
        repo.setSelected(false);
        repo.setRepoId(1);
        repos.add(repo);
        Repo repo2 = new Repo();
        repo2.setName("TestMavenProject");
        repo2.setSelected(false);
        repo2.setRepoId(2);
        repos.add(repo2);
        return repos;
    }

    /*public List<DependencyDto> getDependenciesForRepo(String repoName) {
        List<DependencyDto> dependencies = dependencyResolver.resolve(new Workspace());
        List<DependencyDto> deps = seedDependencies(); // TODO -> Use dependencyResolver to resolve dependencies?
        // TODO: update dependencies with settings
        return deps;
    }

    private List<DependencyDto> seedDependencies() {
        List<DependencyDto> deps = new ArrayList<>();
        deps.add(new DependencyDto("a group", "an artifact", "a version",false, false));
        deps.add(new DependencyDto( "another group", "another artifact", "another version", false, false));
        deps.add(new DependencyDto( "a group2", "an artifact2", "a version2", false, false));
        deps.add(new DependencyDto( "another group2", "another artifact2", "another version2", false, false));
        return deps;
    }*/

    public RepositoryDto getRepository(int repoId) {
        return modelMapper.map(repoRepository.getByRepoId(repoId), RepositoryDto.class);
    }

    public void updateRepo(RepositoryDto repositoryDto) {
        Repo saved = repoRepository.getByRepoId(repositoryDto.getRepoId());
        saved.setSelected(repositoryDto.isSelected());
        repoRepository.save(saved);
    }

    public void saveSettings(RepositoryDto dto) {
        if (dto.isCronJob()) saveCronJob(dto.getName());
        if (!dto.isCronJob()) removeCronJob(dto.getName());
        if (dto.getReviewer() != null) saveReviewer(dto.getName(), dto.getReviewer());
        // TODO - handle changes in settings
    }

    private void removeCronJob(String name) {
        if (getIsCronJob(settingsRepository.findAllSettingsForDependencies(name))){
            settingsRepository.removeCronJob(name);
        }
    }

    private void saveCronJob(String name) {
        Setting cron = new Setting();
        cron.setRepositoryName(name);
        cron.setKey("cron");
        cron.setValue(String.valueOf(true));
        cron.setType(Setting.SettingsType.CRON);
        settingsRepository.saveSetting(cron);
    }

    private void saveReviewer(String name, String reviewer) {
        Setting rev = new Setting();
        rev.setRepositoryName(name);
        rev.setKey("reviewer");
        rev.setValue(reviewer);
        rev.setType(Setting.SettingsType.REVIEWER);
        settingsRepository.saveSetting(rev);
    }

}
