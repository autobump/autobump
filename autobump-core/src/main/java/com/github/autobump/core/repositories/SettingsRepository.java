package com.github.autobump.core.repositories;

import com.github.autobump.core.model.domain.Setting;

import java.util.List;

public interface SettingsRepository {

    Setting saveSetting(Setting setting);

    List<Setting> saveAllSettings(List<Setting> settings);

    Setting findSettingForReviewer(String repoName);

    List<Setting> findAllSettingsForDependencies(String repoName);

    Setting getCronSetting(String repoName);

    void removeCronJob(String repoName);

    List<Setting> getAllIgnores();

    void deleteAll();
}
