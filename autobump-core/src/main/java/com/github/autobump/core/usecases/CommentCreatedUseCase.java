package com.github.autobump.core.usecases;

import com.github.autobump.core.events.CommentCreatedEvent;
import com.github.autobump.core.model.domain.Setting;
import com.github.autobump.core.repositories.SettingsRepository;
import lombok.Builder;

@Builder
public class CommentCreatedUseCase {
    SettingsRepository settingsRepository;
    CommentCreatedEvent event;

    public Setting doHandle(){
        String comment = event.getComment();
        Setting setting = null;
        if ("Ignore this major".equalsIgnoreCase(comment)){
            setting = ignoreMajor(event);
        }
        else if ("Ignore this minor".equalsIgnoreCase(comment)){
            setting = ignoreMinor(event);
        }
        return setting;
    }

    private Setting ignoreMajor(CommentCreatedEvent commentCreatedEvent) {
        Setting setting = extractSettingFromComment(commentCreatedEvent.getPullRequestTitle(), "Major",
                commentCreatedEvent.getRepositoryName());
        IgnoreMajorUseCase ignoreMajorUseCase = IgnoreMajorUseCase
                .builder()
                .settingsRepository(settingsRepository)
                .build();
        ignoreMajorUseCase.addIgnoreMajorSetting(setting);
        return setting;
    }

    private Setting ignoreMinor(CommentCreatedEvent commentCreatedEvent){
        Setting setting = extractSettingFromComment(commentCreatedEvent.getPullRequestTitle(), "Minor",
                commentCreatedEvent.getRepositoryName());
        IgnoreMinorUseCase ignoreMinorUseCase = IgnoreMinorUseCase
                .builder()
                .settingsRepository(settingsRepository)
                .build();
        ignoreMinorUseCase.addIgnoreMinorSetting(setting);
        return setting;
    }

    private Setting extractSettingFromComment(String pullRequestTitle, String type, String repoName) {
        String [] elements = pullRequestTitle.split(" ");
        return Setting.builder()
                .key(String.format("%s:%s", elements[1], elements[4]))
                .value(type)
                .repositoryName(repoName)
                .type(Setting.SettingsType.IGNORE)
                .build();
    }
}
