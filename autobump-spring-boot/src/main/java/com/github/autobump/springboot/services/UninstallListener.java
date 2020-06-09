package com.github.autobump.springboot.services;

import com.atlassian.connect.spring.AddonUninstalledEvent;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.github.autobump.core.model.RepoRepository;
import com.github.autobump.core.model.SettingsRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class UninstallListener implements ApplicationListener<AddonUninstalledEvent> {
    private final AtlassianHostRepository repository;
    private final SettingsRepository settingsRepository;
    private final RepoRepository repoRepository;

    public UninstallListener(AtlassianHostRepository repository, SettingsRepository settingsRepository,
                             RepoRepository repoRepository) {
        this.repository = repository;
        this.settingsRepository = settingsRepository;
        this.repoRepository = repoRepository;
    }

    @Override
    public void onApplicationEvent(AddonUninstalledEvent event) {
        repository.delete(event.getHost());
        repoRepository.deleteAll();
        settingsRepository.deleteAll();
    }
}
