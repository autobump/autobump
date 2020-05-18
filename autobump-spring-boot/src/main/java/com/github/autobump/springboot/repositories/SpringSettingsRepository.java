package com.github.autobump.springboot.repositories;

import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringSettingsRepository implements SettingsRepository {
    @Autowired
    private JpaSettingsRepository jpaSettingsRepository;

    @Override
    public Setting saveSetting(Setting setting) {
        return jpaSettingsRepository.save(setting);
    }

    @Override
    public List<Setting> saveAllSettings(List<Setting> settings) {
        return jpaSettingsRepository.saveAll(settings);
    }
}
