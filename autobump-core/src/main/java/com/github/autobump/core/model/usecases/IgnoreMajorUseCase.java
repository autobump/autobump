package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import lombok.Builder;


@Builder
public class IgnoreMajorUseCase {
    SettingsRepository settingsRepository;

    public void addIgnoreMajorSetting(Setting setting){
        settingsRepository.saveSetting(setting);
    }
}
