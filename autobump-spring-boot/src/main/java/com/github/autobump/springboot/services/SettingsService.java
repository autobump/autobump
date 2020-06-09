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
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Transactional
@Service
@Setter
public class SettingsService {
    private final Autobumpconfig autobumpconfig;
    private final RepoRepository repoRepository;
    private final SpringSettingsRepository settingsRepository;
    private final ModelMapper modelMapper;

    public SettingsService(Autobumpconfig autobumpconfig, RepoRepository repoRepository,
                           SpringSettingsRepository settingsRepository, ModelMapper modelMapper) {
        this.autobumpconfig = autobumpconfig;
        this.repoRepository = repoRepository;
        this.settingsRepository = settingsRepository;
        this.modelMapper = modelMapper;
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
            } else {
                updated.add(isAlreadySaved);
            }
        }
        return updated;
    }

    private List<Repo> addAllRepos(List<Repo> remoteRepos) {
        List<Repo> updated = new ArrayList<>(remoteRepos);
        for (Repo repo : remoteRepos
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

    public RepositoryDto getSettingsForRepository(String repoName, String repoId) {
        List<Setting> settings = settingsRepository.findAllSettingsForDependencies(repoName);
        return getRepositoryDto(repoName, repoId, settings);
    }

    private RepositoryDto getRepositoryDto(String repoName, String repoId, List<Setting> settings) {
        RepositoryDto dto = new RepositoryDto();
        dto.setRepoId(repoId);
        dto.setDependencies(getIgnoredDependenciesFromSettings(settings));
        String name = getReviewerName(repoId, getReviewerFromSetting(settings));
        dto.setReviewer(name);
        dto.setCronJob(getIsCronJob(settings));
        dto.setName(repoName);
        return dto;
    }

    public Set<String> getContributerNamesFromWorkspace(String repoId) {
        var repo = getRepo(repoId);
        Map<String, String> members = new HashMap<>();
        members.put("none", "none");
        members.putAll(autobumpconfig.getGitProvider().getMembersFromWorkspace(repo));
        String currentUserUuid = autobumpconfig.getGitProvider().getCurrentUserUuid();
        return members.entrySet().stream().filter(m -> !m.getValue().equals(currentUserUuid))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());
    }

    private String getReviewerName(String repoId, String uuid) {
        var repo = getRepo(repoId);
        Map<String, String> members = autobumpconfig.getGitProvider().getMembersFromWorkspace(repo);
        String name = "none";
        if (!members.isEmpty()) {
            var optionalName = members.entrySet()
                    .stream()
                    .filter(entry -> Objects.equals(entry.getValue(), uuid))
                    .map(Map.Entry::getKey)
                    .findFirst();
            if (optionalName.isPresent()) {
                name = optionalName.get();
            }
        }
        return name;
    }

    private String getReviewerFromSetting(List<Setting> settings) {
        Setting reviewer = settings
                .stream()
                .filter(s -> s.getType().equals(Setting.SettingsType.REVIEWER))
                .findFirst().orElse(null);
        if (reviewer != null) {
            return reviewer.getValue();
        }
        return "none";
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
            if (dep != null) {
                dep.setIgnoreMajor(setting.getValue().equals("Major"));
                dep.setIgnoreMinor(setting.getValue().equals("Minor"));
                dtos.add(dep);
            }
        }
        return dtos;
    }

    private DependencyDto extractDependencyFromSettingKey(String key, String value) {
        String[] elements = key.split(":");
        if (elements.length > 2) {
            return getDependencyDto(value, elements);
        }
        return null;
    }

    private DependencyDto getDependencyDto(String value, String... elements) {
        DependencyDto dep = new DependencyDto();
        dep.setGroupName(elements[0]);
        dep.setArtifactId(elements[1]);
        dep.setVersionNumber(elements[2]);
        if ("Major".equals(value)) {
            dep.setIgnoreMajor(true);
        }
        if ("Minor".equals(value)) {
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
        if (!dto.getReviewer().equals("none")) {
            Repo repo = getRepo(dto.getRepoId());
            Map<String, String> members = autobumpconfig.getGitProvider().getMembersFromWorkspace(repo);
            if (!members.isEmpty()) {
                String uuid = members.get(dto.getReviewer());
                saveReviewer(dto.getName(), uuid);
            }
        }
    }

    private void saveCronJob(boolean cronJob, String name) {
        if (cronJob) {
            Setting cron = new Setting();
            cron.setRepositoryName(name);
            cron.setKey("cron");
            cron.setValue(String.valueOf(true));
            cron.setType(Setting.SettingsType.CRON);
            settingsRepository.saveSetting(cron);
        } else {
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
        return getSettingsForRepository(name, repoId);
    }

    public void updateSelectedFieldsOfRepos(@ModelAttribute RepositoryListDto dto) {
        for (RepositoryDto repo : dto.getRepositories()
        ) {
            updateRepo(repo);
        }
    }
}
