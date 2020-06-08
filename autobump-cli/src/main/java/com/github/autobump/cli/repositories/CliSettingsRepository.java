package com.github.autobump.cli.repositories;

import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CliSettingsRepository implements SettingsRepository {

    private final List<Setting> settingList;

    public CliSettingsRepository() {
        settingList = new ArrayList<>();
    }

    @Override
    public Setting saveSetting(Setting setting) {
        settingList.add(setting);
        return setting;
    }

    @Override
    public List<Setting> saveAllSettings(List<Setting> settings) {
        settingList.addAll(settings);
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
        // unused
    }

    @Override
    public List<Setting> getAllIgnores() {
        return settingList.stream()
                .filter(s -> s.getType().equals(Setting.SettingsType.IGNORE))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void deleteAll() {
        // unused
    }
}
