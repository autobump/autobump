package com.github.autobump.springboot.repositories;

import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.autobump.core.model.Setting.SettingsType.IGNORE;

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
    public Setting findSettingForReviewer(String repoName) {
        return jpaSettingsRepository.findByTypeAndRepositoryName(Setting.SettingsType.REVIEWER, repoName);
    }

    @Override
    public List<Setting> findAllSettingsForDependencies(String repoName) {
        return jpaSettingsRepository.findAllByRepositoryName(repoName);
    }

    @Override
    public Setting getCronSetting(String repoName) {
        return jpaSettingsRepository.findByTypeAndRepositoryName(Setting.SettingsType.CRON, repoName);
    }

    @Transactional
    @Override
    public void removeCronJob(String repoName){
        jpaSettingsRepository.deleteByTypeAndRepositoryName(Setting.SettingsType.CRON, repoName);
    }

    @Override
    public List<Setting> getAllIgnores() {
        return jpaSettingsRepository.findAll()
                .stream()
                .filter(setting -> setting.getType().equals(IGNORE))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void deleteAll() {
        jpaSettingsRepository.deleteAll();
    }
}
