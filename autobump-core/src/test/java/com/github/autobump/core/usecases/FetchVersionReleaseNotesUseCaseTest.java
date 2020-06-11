package com.github.autobump.core.usecases;

import com.github.autobump.core.model.domain.ReleaseNotes;
import com.github.autobump.core.model.domain.Bump;
import com.github.autobump.core.model.domain.Dependency;
import com.github.autobump.core.model.domain.Version;
import com.github.autobump.core.model.releasenotes.ReleaseNotesSource;
import com.github.autobump.core.repositories.VersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class FetchVersionReleaseNotesUseCaseTest {

    @Mock
    private VersionRepository versionRepository;

    @Mock
    private ReleaseNotesSource releaseNotesSource;

    private Bump bump;

    @BeforeEach
    void setUp() {
    }

    private void initializeTestBump() {
        bump = new Bump(Dependency.builder()
                .group("org.springframework.boot")
                .name("spring-boot-dependencies")
                .version(new FetchVersionReleaseNotesUseCaseTest.TestVersion("1.0.0"))
                .build(),
                new FetchVersionReleaseNotesUseCaseTest.TestVersion("2.2.4.RELEASE"));
    }

    @Test
    void fetchVersionReleaseNotes_supportedReleaseNotesSource_github() {
        initializeTestBump();
        setupVersionRepositoryMock();
        String result = FetchVersionReleaseNotesUseCase.builder()
                .bump(bump)
                .versionRepository(versionRepository)
                .releaseNotesSource(releaseNotesSource)
                .build()
                .fetchVersionReleaseNotes();
        assertThat(result).contains("Autobump found release notes for" +
                " org.springframework.boot:spring-boot-dependencies 2.2.4.RELEASE");
        assertThat(result).contains("RELEASE NOTES");
        assertThat(result).contains("Release notes sample text");
        assertThat(result).contains("Source: http://test.com\n");
    }

    @Test
    void fetchVersionReleaseNotes_unsupportedReleaseNotesSource_gitlab() {
        initializeTestBump();
        setupNoResultVersionRepositoryMock();
        String result = FetchVersionReleaseNotesUseCase.builder()
                .bump(bump)
                .versionRepository(versionRepository)
                .releaseNotesSource(releaseNotesSource)
                .build()
                .fetchVersionReleaseNotes();
        assertThat(result).isEqualTo("");
    }

    private void setupVersionRepositoryMock() {
        when(versionRepository.getScmUrlForDependencyVersion(any(), any()))
                .thenReturn("https://github.com/spring-projects/spring-boot");
        when(releaseNotesSource.getReleaseNotes(any(),any()))
                .thenReturn(new ReleaseNotes("http://test.com","2.2.4.RELEASE",
                        "RELEASE NOTES\nRelease notes sample text"));
    }

    private void setupNoResultVersionRepositoryMock() {
        when(versionRepository.getScmUrlForDependencyVersion(any(), any()))
                .thenReturn("https://gitlab.com/spring-projects/spring-boot");
    }

    private static class TestVersion implements Version {
        private final String version;

        TestVersion(String version) {
            this.version = version;
        }

        @Override
        public String getVersionNumber() {
            return version;
        }

        @Override
        public UpdateType getUpdateType(Version otherVersion) {
            return null;
        }

        @Override
        public int compareTo(Version o) {
            return 1;
        }
    }
}
