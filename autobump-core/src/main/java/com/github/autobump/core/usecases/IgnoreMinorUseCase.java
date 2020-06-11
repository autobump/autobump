package com.github.autobump.core.usecases;

import com.github.autobump.core.model.domain.Setting;
import com.github.autobump.core.repositories.SettingsRepository;
import lombok.Builder;

@Builder
public class IgnoreMinorUseCase {
    SettingsRepository settingsRepository;

    public void addIgnoreMinorSetting(Setting setting){
        settingsRepository.saveSetting(setting);
    }
}
