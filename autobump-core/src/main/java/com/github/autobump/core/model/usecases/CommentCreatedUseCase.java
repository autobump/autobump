package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.core.model.events.CommentCreatedEvent;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CommentCreatedUseCase {
    private final SettingsRepository settingsRepository;
    private final IgnoreMajorUseCase ignoreMajorUseCase;
    private final IgnoreMinorUseCase ignoreMinorUseCase;

    public Setting doHandle(CommentCreatedEvent event){
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
        ignoreMajorUseCase.addIgnoreMajorSetting(setting);
        return setting;
    }

    private Setting ignoreMinor(CommentCreatedEvent commentCreatedEvent){
        Setting setting = extractSettingFromComment(commentCreatedEvent.getPullRequestTitle(), "Minor",
                commentCreatedEvent.getRepositoryName());
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
