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
    private final CommentCreatedUseCase commentCreatedUseCase;
    private final PullRequestClosedUseCase pullRequestClosedUseCase;
    private final RebaseUseCase rebaseUseCase;

    public WebhookService(SettingsRepository settingsRepository, Autobumpconfig config, CommentCreatedUseCase commentCreatedUseCase, PullRequestClosedUseCase pullRequestClosedUseCase, RebaseUseCase rebaseUseCase) {
        this.settingsRepository = settingsRepository;
        this.autobumpconfig = config;
        this.commentCreatedUseCase = commentCreatedUseCase;
        this.pullRequestClosedUseCase = pullRequestClosedUseCase;
        this.rebaseUseCase = rebaseUseCase;
    }

    public void handleComment(String prTitle, String comment, String reponame) {
        if (prTitle.toLowerCase(Locale.US).startsWith("bumped")) {
            var event = CommentCreatedEvent.builder()
                    .comment(comment)
                    .pullRequestTitle(prTitle)
                    .repositoryName(reponame)
                    .build();
            commentCreatedUseCase.doHandle(event);
        }
    }

    public void handleReject(String prTitle, String repoName) {
        if (prTitle.toLowerCase(Locale.US).startsWith("bumped")) {
            var event = PrClosedEvent.builder()
                    .prName(prTitle)
                    .repoName(repoName)
                    .build();
            pullRequestClosedUseCase.doClose(event);
        }
    }

    public void handlePush(String branchname, URI gitUri) {
        if ("master".equalsIgnoreCase(branchname)) {
            try {
//                var config = autobumpconfig.setupConfig();
                var event = new PushEvent(gitUri);
//                RebaseUseCase.builder()
//                        .config(config)
//                        .build()
                rebaseUseCase.handlePushEvent(event);
            } catch (IllegalArgumentException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn(String.format("%s no rebase executed", e.getMessage()));
                }
            }
        }
    }
}
