package com.github.autobump.cli.model;

import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;

import javax.inject.Named;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Named
public class CliSettingsRepository implements SettingsRepository {

    private final Set<Setting> settingSet;

    public CliSettingsRepository() {
        this.settingSet = new HashSet<>();
    }

    @Override
    public Setting saveSetting(Setting setting) {
        settingSet.add(setting);
        return setting;
    }

    @Override
    public List<Setting> saveAllSettings(List<Setting> settings) {
        settingSet.addAll(settings);
        return settings;
    }
}
