package com.github.autobump.springboot.services;

import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.Repo;
import com.github.autobump.core.model.Setting;
import com.github.autobump.springboot.configuration.Autobumpconfig;
import com.github.autobump.springboot.controllers.dtos.DependencyDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import com.github.autobump.springboot.repositories.RepoRepository;
import com.github.autobump.springboot.repositories.SpringSettingsRepository;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Setter
@Transactional
public class SettingsService {
    private final Autobumpconfig autobumpconfig;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    SpringSettingsRepository settingsRepository;
    @Autowired
    RepoRepository repoRepository;

    public SettingsService(Autobumpconfig autobumpconfig) {
        this.autobumpconfig = autobumpconfig;
    }

    public List<RepositoryDto> getAllRepositories(){
        GitProvider gitProvider = autobumpconfig.getGitProvider();
        List<Repo> remoteRepos = gitProvider.getRepos();
        List<Repo> savedRepos = repoRepository.findAll();
        savedRepos = addNewRemoteRepos(remoteRepos, savedRepos);
        savedRepos = removeReposNoLongerRemotelyPresent(remoteRepos, savedRepos);
        return savedRepos
                .stream()
                .map(r -> modelMapper.map(r, RepositoryDto.class))
                .collect(Collectors.toList());
    }

    private List<Repo> addNewRemoteRepos(List<Repo> remoteRepos, List<Repo> savedRepos) {
        if (savedRepos.isEmpty()) {
            savedRepos = new ArrayList<>();
            savedRepos.addAll(remoteRepos);
            for (Repo repo: remoteRepos
                 ) {
                saveRepo(repo);
            }
        } else {
            for (Repo repo : remoteRepos
            ) {
                Repo isAlreadySaved = savedRepos
                        .stream()
                        .filter(s -> s.getRepoId().equals(repo.getRepoId()))
                        .findAny().orElse(null);
                if (isAlreadySaved == null){
                    savedRepos.add(repo);
                    saveRepo(repo);
                }
            }
        }
        return savedRepos;
    }

    public void saveRepo(Repo repo) {
        repoRepository.save(repo);
    }

    private List<Repo> removeReposNoLongerRemotelyPresent(List<Repo> remoteRepos, List<Repo> savedRepos) {
        List<Repo> updatedList = new ArrayList<>();
        for (Repo repo : savedRepos
            ) {
            Repo stillExistsRemotely = remoteRepos
                    .stream()
                    .filter(r -> r.getRepoId().equals(repo.getRepoId()))
                    .findAny().orElse(null);
            if (stillExistsRemotely == null) {
                repoRepository.delete(repo);
            }
            else {
                updatedList.add(repo);
            }
        }
        return updatedList;
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
        dto.setDependencies(dependencyDtos);
        dto.setReviewer(reviewer);
        dto.setCronJob(cronJob);
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
            DependencyDto dep = extractDependencyFromSettingKey(setting.getKey());
            dep.setIgnoreMajor(setting.getValue().equals("major"));
            dep.setIgnoreMinor(setting.getValue().equals("minor"));
            dtos.add(dep);
        }
        return dtos;
    }

    private DependencyDto extractDependencyFromSettingKey(String key) {
        DependencyDto dep = new DependencyDto();
        String[] elements = key.split(":");
        dep.setGroupName(elements[0]);
        dep.setArtifactId(elements[1]);
        dep.setVersionNumber(elements[2]);
        return dep;
    }

    public RepositoryDto getRepository(String repoId) {
        return modelMapper.map(repoRepository.getByRepoId(repoId), RepositoryDto.class);
    }

    public void updateRepo(RepositoryDto repositoryDto) {
        Repo saved = repoRepository.getByRepoId(repositoryDto.getRepoId());
        saved.setSelected(repositoryDto.isSelected());
        saveRepo(saved);
    }

    public void saveSettings(RepositoryDto dto) {
        if (dto.isCronJob()) saveCronJob(dto.getName());
        if (!dto.isCronJob()) removeCronJob(dto.getName());
        if (dto.getReviewer() != null) saveReviewer(dto.getName(), dto.getReviewer());
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

    public Repo getRepo(String repoId) {
        return repoRepository.getByRepoId(repoId);
    }
}
