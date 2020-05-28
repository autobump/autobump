package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.ReleaseNotesSource;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.VersionRepository;
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
                .version(new FetchVersionReleaseNotesUseCaseTest.TestVersion("oldVersionNumber"))
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
        assertThat(result).isEqualTo("RELEASE NOTES\nRelease not sample text\n");
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
                .thenReturn("RELEASE NOTES\nRelease not sample text");
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
