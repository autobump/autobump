package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.core.model.events.CommentCreatedEvent;
import lombok.Builder;

@Builder
public class CommentCreatedUseCase {
    SettingsRepository settingsRepository;

    public Setting handleComment(CommentCreatedEvent commentEvent){
        String comment = commentEvent.getComment();
        String pullRequestTitle = commentEvent.getPullRequestTitle();
        Setting setting = null;
        if ("Ignore this major".equalsIgnoreCase(comment)){
            setting = ignoreMajor(pullRequestTitle);
        }
        else if ("Ignore this minor".equalsIgnoreCase(comment)){
            setting = ignoreMinor(pullRequestTitle);
        }
        return setting;
    }

    private Setting ignoreMajor(String pullRequestTitle) {
        Setting setting = extractSettingFromComment(pullRequestTitle, "Major");
        IgnoreMajorUseCase ignoreMajorUseCase = IgnoreMajorUseCase
                .builder()
                .settingsRepository(settingsRepository)
                .build();
        ignoreMajorUseCase.addIgnoreMajorSetting(setting);
        return setting;
    }

    private Setting ignoreMinor(String pullRequestTitle){
        Setting setting = extractSettingFromComment(pullRequestTitle, "Minor");
        IgnoreMinorUseCase ignoreMinorUseCase = IgnoreMinorUseCase
                .builder()
                .settingsRepository(settingsRepository)
                .build();
        ignoreMinorUseCase.addIgnoreMinorSetting(setting);
        return setting;
    }

    private Setting extractSettingFromComment(String pullRequestTitle, String type) {
        String [] elements = pullRequestTitle.split(" ");
        return Setting.builder()
                .key(String.format("%s:%s", elements[1], elements[4]))
                .value(type)
                .type(Setting.SettingsType.IGNORE)
                .build();
    }
}
