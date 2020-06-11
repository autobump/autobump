package com.github.autobump.springboot.services;

import com.atlassian.connect.spring.AtlassianHostRepository;
import com.github.autobump.core.model.domain.Repo;
import com.github.autobump.core.repositories.RepoRepository;
import com.github.autobump.core.repositories.SettingsRepository;
import com.github.autobump.core.usecases.AutobumpUseCase;
import com.github.autobump.github.model.GithubReleaseNotesSource;
import com.github.autobump.springboot.configuration.Autobumpconfig;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.stream.Collectors;


@Service
@Setter
public class AutoBumpService {
    private final Logger logger = LoggerFactory.getLogger(AutoBumpService.class);

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private RepoRepository repoRepository;

    private AtlassianHostRepository repository;

    private Autobumpconfig autobumpconfig;

    public AutoBumpService(AtlassianHostRepository repository,
                           Autobumpconfig autobumpconfig) {
        this.repository = repository;
        this.autobumpconfig = autobumpconfig;
    }

    @Scheduled(fixedRate = 86_400_000L)
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public void autoBump() {
        repository.findAll().forEach(atlassianHost -> {
            var repos = repoRepository
                    .findAll()
                    .stream()
                    .filter(r -> r.isSelected() && isCronJob(r))
                    .map(Repo::getLink)
                    .collect(Collectors.toUnmodifiableList());
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

    private boolean isCronJob(Repo repo){
        return settingsRepository.getCronSetting(repo.getName()) != null;
    }

    @Async
    public void executeAutoBump(String repo) {
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
