package com.github.autobump.springboot.services;

import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.Repo;
import com.github.autobump.core.model.RepoRepository;
import com.github.autobump.core.model.Setting;
import com.github.autobump.springboot.configuration.Autobumpconfig;
import com.github.autobump.springboot.controllers.dtos.DependencyDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import com.github.autobump.springboot.controllers.dtos.RepositoryListDto;
import com.github.autobump.springboot.repositories.SpringSettingsRepository;
import lombok.Setter;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
            DependencyDto dep = extractDependencyFromSettingKey(setting.getKey(), setting.getValue());
            if (dep != null){
                dep.setIgnoreMajor(setting.getValue().equals("Major"));
                dep.setIgnoreMinor(setting.getValue().equals("Minor"));
                dtos.add(dep);
            }
        }
        return dtos;
    }

    private DependencyDto extractDependencyFromSettingKey(String key, String value) {
        String[] elements = key.split(":");
        if (elements.length > 2){
            return getDependencyDto(value, elements);
        }
        return null;
    }

    private DependencyDto getDependencyDto(String value, String... elements) {
        DependencyDto dep = new DependencyDto();
        dep.setGroupName(elements[0]);
        dep.setArtifactId(elements[1]);
        dep.setVersionNumber(elements[2]);
        if ("Major".equals(value)){
            dep.setIgnoreMajor(true);
        }
        if ("Minor".equals(value)){
            dep.setIgnoreMinor(true);
        }
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

    public RepositoryDto getRepositoryDtoWithSettings(String repoId) {
        String name = getRepo(repoId).getName();
        return getSettingsForRepository(name);
    }

    public void updateSelectedFieldsOfRepos(@ModelAttribute RepositoryListDto dto) {
        for (RepositoryDto repo: dto.getRepositories()
        ) {
            updateRepo(repo);
        }
    }

    public Set<String> getReviewerNames(String repoId) {
        var repo = getRepo(repoId);
        Map<String, String> members = autobumpconfig.getGitProvider().getMembersFromWorkspace(repo);
        return members.keySet();
    }
}
