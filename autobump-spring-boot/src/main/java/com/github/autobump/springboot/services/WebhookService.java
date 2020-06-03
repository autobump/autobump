package com.github.autobump.springboot.services;

import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.core.model.events.CommentCreatedEvent;
import com.github.autobump.core.model.events.PrClosedEvent;
import com.github.autobump.core.model.events.PushEvent;
import com.github.autobump.core.model.usecases.CommentCreatedUseCase;
import com.github.autobump.core.model.usecases.PullRequestClosedUseCase;
import com.github.autobump.core.model.usecases.RebaseUseCase;
import com.github.autobump.springboot.configuration.Autobumpconfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Locale;

@Service
public class WebhookService {
    private final SettingsRepository settingsRepository;
    private final Autobumpconfig autobumpconfig;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public WebhookService(SettingsRepository settingsRepository, Autobumpconfig config) {
        this.settingsRepository = settingsRepository;
        this.autobumpconfig = config;
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

    public void handlePush(String branchname, URI gitUri) {
        if ("master".equalsIgnoreCase(branchname)) {
            try {
                var config = autobumpconfig.setupConfig();
                var event = new PushEvent(gitUri);
                RebaseUseCase.builder()
                        .event(event)
                        .config(config)
                        .build()
                        .handlePushEvent();
            } catch (IllegalArgumentException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(String.format("%s no rebase executed", e.getMessage()));
                }
            }
        }
    }
}
