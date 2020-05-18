package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import lombok.Builder;

@Builder
public class IgnoreMinorUseCase {
    SettingsRepository settingsRepository;

    public void addIgnoreMinorSetting(Setting setting){
        settingsRepository.saveSetting(setting);
    }
}
