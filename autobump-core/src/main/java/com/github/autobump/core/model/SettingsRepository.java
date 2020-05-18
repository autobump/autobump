package com.github.autobump.core.model;

import java.util.List;

public interface SettingsRepository {

    Setting saveSetting(Setting setting);

    List<Setting> saveAllSettings(List<Setting> settings);
}
