package com.github.autobump.core.model;

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
