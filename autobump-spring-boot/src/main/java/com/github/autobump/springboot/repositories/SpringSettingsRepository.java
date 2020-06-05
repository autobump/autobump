package com.github.autobump.springboot.repositories;

import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

import java.util.List;

@Repository
public class SpringSettingsRepository implements SettingsRepository {
    private final JpaSettingsRepository jpaSettingsRepository;

    public SpringSettingsRepository(JpaSettingsRepository jpaSettingsRepository) {
        this.jpaSettingsRepository = jpaSettingsRepository;
    }

    @Override
    public Setting saveSetting(Setting setting) {
        return jpaSettingsRepository.save(setting);
    }

    @Override
    public List<Setting> saveAllSettings(List<Setting> settings) {
        return jpaSettingsRepository.saveAll(settings);
    }

    @Override
    public List<Setting> findAllSettingsForDependencies(String repoName) {
        return jpaSettingsRepository.findAllByRepositoryName(repoName);
    }

    @Transactional
    @Override
    public void removeCronJob(String repoName){
        jpaSettingsRepository.deleteByTypeAndRepositoryName(Setting.SettingsType.CRON, repoName);
    }
}
