package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public class PullRequestClosedUseCase {
    private final SettingsRepository settingsRepository;
    private final Bump bump;

    public List<Setting> doClose(){
        List<Setting> settings = new ArrayList<>();
        for (Dependency dependency : bump.getDependencies()) {
            settings.add(Setting.builder()
                    .key(String.format("%s:%s", dependency.getGroup(), dependency.getName()))
                    .value(bump.getUpdatedVersion().getVersionNumber())
                    .type(Setting.SettingsType.IGNORE)
                    .build());
        }
        return settingsRepository.saveAllSettings(settings);
    }
}
