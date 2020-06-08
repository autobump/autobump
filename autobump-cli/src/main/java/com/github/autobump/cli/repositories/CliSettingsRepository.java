package com.github.autobump.cli.repositories;

import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CliSettingsRepository implements SettingsRepository {

    private final List<Setting> settingList;

    public CliSettingsRepository() {
        settingList = new ArrayList<>();
    }

    @Override
    public Setting saveSetting(Setting setting) {
        return setting;
    }

    @Override
    public List<Setting> saveAllSettings(List<Setting> settings) {
        return settingList;
    }

    @Override
    public List<Setting> findAllSettingsForDependencies(String repoName) {
        return settingList.stream()
                .filter(s -> s.getRepositoryName().equalsIgnoreCase(repoName))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Setting getCronSetting(String repoName) {
        return null;
    }

    @Override
    public void removeCronJob(String repoName) {

    }

    @Override
    public List<Setting> getAllIgnores() {
        return Collections.emptyList();
    }
}
