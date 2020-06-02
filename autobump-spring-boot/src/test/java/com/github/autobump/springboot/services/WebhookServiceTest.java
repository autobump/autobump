package com.github.autobump.springboot.services;

import com.github.autobump.bitbucket.model.BitBucketUrlHelper;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.IgnoreRepository;
import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.core.model.UseCaseConfiguration;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.springboot.configuration.Autobumpconfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    Autobumpconfig autobumpconfig;

    @Mock
    SettingsRepository settingsRepository;

    @InjectMocks
    WebhookService webhookService;

    @BeforeEach
    void setUp() {
        webhookService = new WebhookService(settingsRepository, autobumpconfig);
        BitBucketUrlHelper urlHelper = Mockito.mock(BitBucketUrlHelper.class);
        Mockito.lenient().when(urlHelper.getOwnerName(any())).thenReturn("test");
        Mockito.lenient().when(urlHelper.getRepoName(any())).thenReturn("test");
        Mockito.lenient().when(autobumpconfig.setupConfig()).thenReturn(UseCaseConfiguration.builder()
                .urlHelper(urlHelper)
                .gitProvider(Mockito.mock(GitProvider.class))
                .versionRepository(Mockito.mock(VersionRepository.class))
                .ignoreRepository(Mockito.mock(IgnoreRepository.class))
                .gitClient(Mockito.mock(GitClient.class))
                .dependencyResolver(Mockito.mock(DependencyResolver.class))
                .dependencyBumper(Mockito.mock(DependencyBumper.class))
                .build());
    }

    @Test
    void handleComment() {
        webhookService.handleComment(
                "Bumped org.projectlombok:lombok:1.18.10 to version: 1.18.12",
                "Ignore this minor",
                "simplemultimoduletestproject");
        verify(settingsRepository, times(1)).saveSetting(any());
    }

    @Test
    void handleReject() {
        webhookService.handleReject(
                "Bumped org.projectlombok:lombok:1.18.10 to version: 1.18.12",
                "simplemultimoduletestproject");
        verify(settingsRepository, times(1)).saveAllSettings(any());
    }

    @Test
    void handlePush() {

        assertThatCode(() -> webhookService.handlePush("master",
                URI.create("https://bitbucket.org/grietvermeesch/simplemultimoduletestproject/pull-requests/53")))
                .doesNotThrowAnyException();
    }
}
