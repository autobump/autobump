package com.github.autobump.springboot.services;

import com.github.autobump.core.model.DependencyResolver;
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
    DependencyResolver dependencyResolver;
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
                .filter(r -> r.isSelected())
                .map(repo -> modelMapper.map(repo, RepositoryDto.class))
                .collect(Collectors.toUnmodifiableList());
    }

    // TODO - should be removed when gitprovider used
    private List<Repo> seed() {
        List<Repo> repos = new ArrayList<>();
        Repo repo = new Repo();
        repo.setName("MultiModuleMavenProject");
        repo.setCronJob(true);
        repo.setSelected(false);
        repo.setRepoId(1);
        repos.add(repo);
        Repo repo2 = new Repo();
        repo2.setName("TestMavenProject");
        repo2.setCronJob(false);
        repo2.setSelected(false);
        repo2.setRepoId(2);
        repos.add(repo2);
        return repos;
    }

    public void doAutoBump(int repoId){
        AutobumpUseCase.builder()
                .config(autobumpconfig.setupConfig())
                .releaseNotesSource(new GithubReleaseNotesSource("https://api.github.com"))
                .uri(URI.create(repoRepository.getByRepoId(repoId).getLink()))
                .build()
                .doAutoBump();
    }

    public List<DependencyDto> getDependenciesForRepo(String repoName) {
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
    }

    public RepositoryDto getRepository(int repoId) {
        return modelMapper.map(repoRepository.getByRepoId(repoId), RepositoryDto.class);
    }

    public Repo updateRepo(RepositoryDto repositoryDto) {
        Repo saved = repoRepository.getByRepoId(repositoryDto.getRepoId());
        saved.setSelected(repositoryDto.isSelected());
        return repoRepository.save(saved);
    }

    public void saveSettings(RepositoryDto repositoryDto){
        saveCronJob(repositoryDto.getName(), repositoryDto.isCronJob());
        saveReviewer(repositoryDto.getName(), repositoryDto.getReviewer());
        saveSettingsForDependencies(repositoryDto.getName(), repositoryDto.getDependencies());
    }

    private void saveCronJob(String name, boolean isCron) {
        Setting cron = new Setting();
        cron.setRepositoryName(name);
        cron.setKey("cron");
        cron.setValue(String.valueOf(isCron));
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

    public void saveSettingsForDependencies(String name, List<DependencyDto> dependencies) {
        for (DependencyDto dep: dependencies
             ) {
            saveSettingsForDependency(name, dep);
        }
    }

    //TODO - should use Usecases!
    private void saveSettingsForDependency(String name, DependencyDto d) {
        if (d.isIgnoreMajor()){
            saveIgnore(name, d, "major");
        }
        if (d.isIgnoreMinor()){
            saveIgnore(name, d, "minor");
        }
    }

    private void saveIgnore(String name, DependencyDto d, String type) {
        Setting setting = new Setting();
        setting.setKey(d.getGroupName() + "/" + d.getArtifactId() + "/" +d.getVersionNumber());
        setting.setRepositoryName(name);
        if (d.isIgnoreMajor())
            setting.setType(Setting.SettingsType.IGNORE);
        setting.setValue(type);
        settingsRepository.saveSetting(setting);
    }
}
