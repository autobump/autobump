package com.github.autobump.core.model;

import java.util.List;

public interface SettingsRepository {

    List<Setting> getSettings();

    Setting saveSetting(Setting setting);

    List<Setting> saveAllSettings(List<Setting> settings);

}
