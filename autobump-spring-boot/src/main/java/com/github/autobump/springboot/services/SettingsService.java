package com.github.autobump.springboot.services;

public class SettingsService {

    private final SpringSettingsRepository springSettingsRepository;

    public SettingsService(SpringSettingsRepository springSettingsRepository) {
        this.springSettingsRepository = springSettingsRepository;
    }
}
