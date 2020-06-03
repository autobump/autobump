package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.core.model.events.CommentCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class CommentCreatedUseCaseTest {
    SettingsRepository settingsRepository;
    CommentCreatedUseCase commentCreatedUseCase;
    CommentCreatedEvent eventMajorIgnore;
    CommentCreatedEvent eventMinorIgnore;
    CommentCreatedEvent eventWronglyFormulated;

    @BeforeEach
    void setUp() {
        settingsRepository = Mockito.mock(SettingsRepository.class);
        String pullRequestTitle = "Bumped com.h2database:h2 to version: 1.4.200";
        eventMajorIgnore = CommentCreatedEvent.builder()
                .pullRequestTitle(pullRequestTitle)
                .comment("Ignore this major")
                .repositoryName("test")
                .build();
        eventMinorIgnore = CommentCreatedEvent.builder()
                .pullRequestTitle(pullRequestTitle)
                .comment("Ignore this minor")
                .repositoryName("test")
                .build();
        eventWronglyFormulated = CommentCreatedEvent.builder()
                .pullRequestTitle(pullRequestTitle)
                .comment("Wrongly formulated comment")
                .build();
        commentCreatedUseCase = CommentCreatedUseCase
                .builder()
                .settingsRepository(settingsRepository)
                .build();
    }

    @Test
    void handleIgnoreMajorComment() {
        commentCreatedUseCase = CommentCreatedUseCase
                .builder()
                .event(eventMajorIgnore)
                .settingsRepository(settingsRepository)
                .build();
        assertThat(commentCreatedUseCase
                .doHandle())
                .isEqualToComparingFieldByField(
                        Setting.builder()
                                .key("com.h2database:h2:1.4.200")
                                .value("Major")
                                .repositoryName("test")
                                .type(Setting.SettingsType.IGNORE).build());
    }

    @Test
    void handleIgnoreMinorComment() {
        commentCreatedUseCase = CommentCreatedUseCase
                .builder()
                .event(eventMinorIgnore)
                .settingsRepository(settingsRepository)
                .build();
        assertThat(commentCreatedUseCase
                .doHandle())
                .isEqualToComparingFieldByField(
                        Setting.builder()
                                .key("com.h2database:h2:1.4.200")
                                .value("Minor")
                                .repositoryName("test")
                                .type(Setting.SettingsType.IGNORE).build());
    }

    @Test
    void handleWronglyFormulatedComment() {
        commentCreatedUseCase = CommentCreatedUseCase
                .builder()
                .event(eventWronglyFormulated)
                .settingsRepository(settingsRepository)
                .build();
        assertThat(commentCreatedUseCase
                .doHandle())
                .isNull();
    }
}
