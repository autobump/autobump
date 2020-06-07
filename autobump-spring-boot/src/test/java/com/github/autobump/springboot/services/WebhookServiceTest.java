package com.github.autobump.springboot.services;

import com.github.autobump.bitbucket.model.BitBucketGitProviderUrlHelper;
import com.github.autobump.core.model.GitProviderUrlHelper;
import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.core.model.usecases.CommentCreatedUseCase;
import com.github.autobump.core.model.usecases.PullRequestClosedUseCase;
import com.github.autobump.core.model.usecases.RebaseUseCase;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
class WebhookServiceTest {

    @Mock
    private Autobumpconfig autobumpconfig;
    @Mock
    private CommentCreatedUseCase commentCreatedUseCase;
    @Mock
    private PullRequestClosedUseCase pullRequestClosedUseCase;
    @Mock
    private RebaseUseCase rebaseUseCase;
    @Mock
    private SettingsRepository settingsRepository;

    @InjectMocks
    private WebhookService webhookService;

    @BeforeEach
    void setUp() {
//        webhookService = new WebhookService(settingsRepository, autobumpconfig, commentCreatedUseCase, pullRequestClosedUseCase, rebaseUseCase);
        GitProviderUrlHelper urlHelper = Mockito.mock(BitBucketGitProviderUrlHelper.class);
        Mockito.lenient().when(urlHelper.getOwnerName(any())).thenReturn("test");
        Mockito.lenient().when(urlHelper.getRepoName(any())).thenReturn("test");
//        Mockito.lenient().when(autobumpconfig.setupConfig()).thenReturn(UseCaseConfiguration.builder()
//                .gitProviderUrlHelper(urlHelper)
//                .gitProvider(Mockito.mock(GitProvider.class))
//                .versionRepository(Mockito.mock(VersionRepository.class))
//                .ignoreRepository(Mockito.mock(IgnoreRepository.class))
//                .gitClient(Mockito.mock(GitClient.class))
//                .dependencyResolver(Mockito.mock(DependencyResolver.class))
//                .dependencyBumper(Mockito.mock(DependencyBumper.class))
//                .build());
    }

    @Test
    void handleComment() {
        webhookService.handleComment(
                "Bumped org.projectlombok:lombok:1.18.10 to version: 1.18.12",
                "Ignore this minor",
                "simplemultimoduletestproject");
//        verify(settingsRepository, times(1)).saveSetting(any());
        verify(commentCreatedUseCase, times(1)).doHandle(any());
    }

    @Test
    void handleCommentNoAutoBumpPr() {
        webhookService.handleComment(
                "developmenttest",
                "Ignore this minor",
                "simplemultimoduletestproject");
//        verify(settingsRepository, times(0)).saveSetting(any());
        verify(commentCreatedUseCase, times(0)).doHandle(any());
    }

    @Test
    void handleReject() {
        webhookService.handleReject(
                "Bumped org.projectlombok:lombok:1.18.10 to version: 1.18.12",
                "simplemultimoduletestproject");
//        verify(settingsRepository, times(1)).saveAllSettings(any());
        verify(pullRequestClosedUseCase, times(1)).doClose(any());
    }

    @Test
    void handleRejectNoAutoBumpPr() {
        webhookService.handleReject(
                "developmenttest",
                "simplemultimoduletestproject");
//        verify(settingsRepository, times(0)).saveAllSettings(any());
        verify(pullRequestClosedUseCase, times(0)).doClose(any());
    }

    @Test
    void handlePush() {
        assertThatCode(() -> webhookService.handlePush("master",
                URI.create("https://bitbucket.org/grietvermeesch/simplemultimoduletestproject/pull-requests/53")))
                .doesNotThrowAnyException();
    }

    @Test
    void handlePushNotOnMaster() {
        assertThatCode(() -> webhookService.handlePush("testy",
                URI.create("https://bitbucket.org/grietvermeesch/simplemultimoduletestproject/pull-requests/53")))
                .doesNotThrowAnyException();
    }

    @Test
    void handlePushIlligalArg() {
//        Mockito.lenient().when(autobumpconfig.setupConfig()).thenThrow(new IllegalArgumentException());
        doThrow(IllegalArgumentException.class).when(rebaseUseCase).handlePushEvent(any());
        assertThatCode(() -> webhookService.handlePush("master",
                URI.create("https://bitbucket.org/grietvermeesch/simplemultimoduletestproject/pull-requests/53")))
                .doesNotThrowAnyException();
    }

}
