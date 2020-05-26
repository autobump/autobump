package com.github.autobump.springboot.services;

import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.core.model.events.CommentCreatedEvent;
import com.github.autobump.core.model.events.PrClosedEvent;
import com.github.autobump.core.model.usecases.CommentCreatedUseCase;
import com.github.autobump.core.model.usecases.PullRequestClosedUseCase;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class WebhookService {
    private final SettingsRepository settingsRepository;

    public WebhookService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public void handleComment(String prTitle, String comment, String reponame) {
        if (prTitle.toLowerCase(Locale.US).startsWith("bumped")) {
            var event = CommentCreatedEvent.builder()
                    .comment(comment)
                    .pullRequestTitle(prTitle)
                    .repositoryName(reponame)
                    .build();
            CommentCreatedUseCase.builder()
                    .settingsRepository(settingsRepository)
                    .event(event)
                    .build()
                    .doHandle();

        }
    }

    public void handleReject(String prTitle, String repoName) {
        if (prTitle.toLowerCase(Locale.US).startsWith("bumped")) {
            var event = PrClosedEvent.builder()
                    .prName(prTitle)
                    .repoName(repoName)
                    .build();
            PullRequestClosedUseCase.builder()
                    .settingsRepository(settingsRepository)
                    .prClosedEvent(event)
                    .build()
                    .doClose();
        }
    }
}
