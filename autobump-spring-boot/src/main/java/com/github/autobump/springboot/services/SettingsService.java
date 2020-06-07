package com.github.autobump.springboot.services;

import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.Repo;
import com.github.autobump.core.model.RepoRepository;
import com.github.autobump.core.model.Setting;
import com.github.autobump.springboot.configuration.Autobumpconfig;
import com.github.autobump.springboot.controllers.dtos.DependencyDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import com.github.autobump.springboot.repositories.SpringSettingsRepository;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Transactional
@Service
@Setter
public class SettingsService {
    @Autowired
    RepoRepository repoRepository;

    @Autowired
    SpringSettingsRepository settingsRepository;

    @Autowired
    ModelMapper modelMapper;

    private final Autobumpconfig autobumpconfig;

    public SettingsService(Autobumpconfig autobumpconfig) {
        this.autobumpconfig = autobumpconfig;
    }

    public List<RepositoryDto> getAllRepositories() {
        GitProvider gitProvider = autobumpconfig.getGitProvider();
        List<Repo> remoteRepos = gitProvider.getRepos();
        List<Repo> savedRepos = repoRepository.findAll();
        List<Repo> updated = addNewRemoteRepos(remoteRepos, savedRepos);
        removeReposNoLongerRemotelyPresent(remoteRepos, savedRepos);
        return updated
                .stream()
                .map(r -> modelMapper.map(r, RepositoryDto.class))
                .collect(Collectors.toList());
    }

    private List<Repo> addNewRemoteRepos(List<Repo> remoteRepos, List<Repo> savedRepos) {
        if (savedRepos.isEmpty()) {
            return addAllRepos(remoteRepos);
        } else {
            return addRemotes(remoteRepos, savedRepos);
        }
    }

    private List<Repo> addRemotes(List<Repo> remoteRepos, List<Repo> savedRepos) {
        List<Repo> updated = new ArrayList<>();
        for (Repo repo : remoteRepos
        ) {
            Repo isAlreadySaved = savedRepos
                    .stream()
                    .filter(s -> s.getRepoId().equals(repo.getRepoId()))
                    .findAny().orElse(null);
            if (isAlreadySaved == null) {
                updated.add(repo);
                repoRepository.save(repo);
            }
            else{
                updated.add(isAlreadySaved);
            }
        }
        return updated;
    }

    private List<Repo> addAllRepos(List<Repo> remoteRepos) {
        List<Repo> updated = new ArrayList<>();
        updated.addAll(remoteRepos);
        for (Repo repo: remoteRepos
             ) {
            repoRepository.save(repo);
        }
        return updated;
    }

    private void removeReposNoLongerRemotelyPresent(List<Repo> remoteRepos, List<Repo> savedRepos) {
        List<Repo> updatedList = new ArrayList<>();
        for (Repo repo : savedRepos
        ) {
            Repo stillExistsRemotely = remoteRepos
                    .stream()
                    .filter(r -> r.getRepoId().equals(repo.getRepoId()))
                    .findAny().orElse(null);
            if (stillExistsRemotely == null) {
                repoRepository.delete(repo);
            } else {
                updatedList.add(repo);
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

    public RepositoryDto getSettingsForRepository(String repoName) {
        List<Setting> settings = settingsRepository.findAllSettingsForDependencies(repoName);
        RepositoryDto dto = new RepositoryDto();
        dto.setDependencies(getIgnoredDependenciesFromSettings(settings));
        dto.setReviewer(getReviewerFromSetting(settings));
        dto.setCronJob(getIsCronJob(settings));
        dto.setName(repoName);
        return dto;
    }

    private String getReviewerFromSetting(List<Setting> settings) {
        String rev = "";
        Setting reviewer = settings
                .stream()
                .filter(s -> s.getType().equals(Setting.SettingsType.REVIEWER))
                .findFirst().orElse(null);
        if (reviewer != null) {
            rev = reviewer.getValue();
        }
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
        for (Setting setting : depSettings
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
        repoRepository.save(saved);
    }

    public void saveSettings(RepositoryDto dto) {
        saveCronJob(dto.isCronJob(), dto.getName());
        if (dto.getReviewer() != null) {
            saveReviewer(dto.getName(), dto.getReviewer());
        }
    }

    private void saveCronJob(boolean cronJob, String name) {
        if (cronJob){
            Setting cron = new Setting();
            cron.setRepositoryName(name);
            cron.setKey("cron");
            cron.setValue(String.valueOf(true));
            cron.setType(Setting.SettingsType.CRON);
            settingsRepository.saveSetting(cron);
        }
        else {
            settingsRepository.removeCronJob(name);
        }
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
