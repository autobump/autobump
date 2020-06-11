package com.github.autobump.core.usecases;

import com.github.autobump.core.events.PrClosedEvent;
import com.github.autobump.core.model.domain.Dependency;
import com.github.autobump.core.model.domain.Setting;
import com.github.autobump.core.repositories.SettingsRepository;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public class PullRequestClosedUseCase {
    private final SettingsRepository settingsRepository;
    private final PrClosedEvent prClosedEvent;

    public List<Setting> doClose(){
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
