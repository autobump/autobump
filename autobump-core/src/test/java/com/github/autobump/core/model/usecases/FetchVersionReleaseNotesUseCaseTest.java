package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.VersionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FetchVersionReleaseNotesUseCaseTest {

    @Mock
    private VersionRepository versionRepository;

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

    private void setupVersionRepositoryMock() {
        when(versionRepository.getScmUrlForDependencyVersion(any(), any()))
                .thenReturn("https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies" +
                        "/2.2.4.RELEASE/spring-boot-dependencies-2.2.4.RELEASE.pom");
    }

    @Test
    void fetchVersionReleaseNotes() {
        initializeTestBump();
        setupVersionRepositoryMock();
        String result = FetchVersionReleaseNotesUseCase.builder()
                .bump(bump)
                .versionRepository(versionRepository)
                .build()
                .fetchVersionReleaseNotes();
        System.out.println(result);
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
