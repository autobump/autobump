package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.core.model.events.PrClosedEvent;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public class PullRequestClosedUseCase {
    private final SettingsRepository settingsRepository;

    public List<Setting> doClose(PrClosedEvent prClosedEvent){
        List<Setting> settings = new ArrayList<>();
        for (Dependency dependency : prClosedEvent.getBump().getDependencies()) {
            settings.add(Setting.builder()
                    .key(String.format("%s:%s", dependency.getGroup(), dependency.getName()))
                    .value(prClosedEvent.getBump().getUpdatedVersion().getVersionNumber())
                    .type(Setting.SettingsType.IGNORE)
                    .repositoryName(prClosedEvent.getRepoName())
                    .build());
        }
        return settingsRepository.saveAllSettings(settings);
    }
}
