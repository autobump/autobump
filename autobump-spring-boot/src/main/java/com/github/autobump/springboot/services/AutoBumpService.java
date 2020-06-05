package com.github.autobump.springboot.services;

import com.atlassian.connect.spring.AtlassianHostRepository;
import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.core.model.usecases.AutobumpUseCase;
import com.github.autobump.github.model.GithubReleaseNotesSource;
import com.github.autobump.springboot.configuration.Autobumpconfig;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;


@Service
@Setter
public class AutoBumpService {
    private final Logger logger = LoggerFactory.getLogger(AutoBumpService.class);

    private AtlassianHostRepository repository;

    private Autobumpconfig autobumpconfig;

    private final SettingsRepository settingsRepository;

    public AutoBumpService(AtlassianHostRepository repository, Autobumpconfig autobumpconfig, SettingsRepository settingsRepository) {
        this.repository = repository;
        this.autobumpconfig = autobumpconfig;
        this.settingsRepository = settingsRepository;
    }

    @Scheduled(fixedRate = 86_400_000L)
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void autoBump() {
        repository.findAll().forEach(atlassianHost -> {
            var repos = autobumpconfig.getGitProvider().getRepos();
            for (String repo : repos) {
                try {
                    executeAutoBump(repo);
                } catch (RuntimeException e) {
                    if (logger.isErrorEnabled()) {
                        logger.error(String.format("Something went wrong while bumping: %s", repo), e);
                    }
                }
            }
        });
    }

    private void executeAutoBump(String repo) {
        var result = AutobumpUseCase.builder()
                .config(autobumpconfig.setupConfig())
                .releaseNotesSource(new GithubReleaseNotesSource("https://api.github.com"))
                .uri(URI.create(repo))
                .settingsRepository(settingsRepository)
                .build()
                .doAutoBump();
        if (logger.isInfoEnabled()) {
            logger.info(String.format("bumped repo: %s, number of bumps: %d",
                    repo,
                    result.getNumberOfBumps()));
        }
    }
}
