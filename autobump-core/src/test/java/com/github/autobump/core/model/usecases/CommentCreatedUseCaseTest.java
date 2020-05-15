package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.core.model.events.CommentCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatCode;

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
                .build();
        eventMinorIgnore = CommentCreatedEvent.builder()
                .pullRequestTitle(pullRequestTitle)
                .comment("Ignore this minor")
                .build();
        eventWronglyFormulated = CommentCreatedEvent.builder()
                .pullRequestTitle(pullRequestTitle)
                .comment("wrongly forulated comment")
                .build();
        commentCreatedUseCase = CommentCreatedUseCase
                .builder()
                .settingsRepository(settingsRepository)
                .build();

    }

    @Test
    void handleIgnoreMajorComment() {
        assertThatCode(() -> commentCreatedUseCase
                .handleComment(eventMajorIgnore))
                .doesNotThrowAnyException();
    }

    @Test
    void handleIgnoreMinorComment(){
        assertThatCode(() -> commentCreatedUseCase
                .handleComment(eventMinorIgnore))
                .doesNotThrowAnyException();
    }

    @Test
    void handleWronglyFormulatedComment(){
        assertThatCode(() -> commentCreatedUseCase
                .handleComment(eventWronglyFormulated))
                .doesNotThrowAnyException();
    }
}
