package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.core.model.events.CommentCreatedEvent;
import lombok.Builder;

@Builder
public class CommentCreatedUseCase {
    SettingsRepository settingsRepository;

    public void handleComment(CommentCreatedEvent commentEvent){
        String comment = commentEvent.getComment();
        String pullRequestTitle = commentEvent.getPullRequestTitle();
        if ("Ignore this major".equalsIgnoreCase(comment)){
            ignoreMajor(pullRequestTitle);
        }
        else if ("Ignore this minor".equalsIgnoreCase(comment)){
            ignoreMinor(pullRequestTitle);
        }
    }

    private void ignoreMajor(String pullRequestTitle) {
        Setting setting = extractSettingFromComment(pullRequestTitle, "Major");
        IgnoreMajorUseCase ignoreMajorUseCase = IgnoreMajorUseCase
                .builder()
                .settingsRepository(settingsRepository)
                .build();
        ignoreMajorUseCase.addIgnoreMajorSetting(setting);
    }

    private void ignoreMinor(String pullRequestTitle){
        Setting setting = extractSettingFromComment(pullRequestTitle, "Minor");
        IgnoreMinorUseCase ignoreMinorUseCase = IgnoreMinorUseCase
                .builder()
                .settingsRepository(settingsRepository)
                .build();
        ignoreMinorUseCase.addIgnoreMinorSetting(setting);
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
