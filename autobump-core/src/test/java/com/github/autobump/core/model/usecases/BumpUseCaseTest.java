package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThatCode;

class BumpUseCaseTest {
    private DependencyBumper dependencyBumper;
    private Workspace workspace;
    private Dependency dependency;
    private Version latestVersion;

    @BeforeEach
    void setUp() {
        dependencyBumper = Mockito.mock(DependencyBumper.class);
        workspace = new Workspace("");
        dependency = Dependency.builder().build();
        latestVersion = new TestVersion();
    }

    @Test
    void doBump() {
        assertThatCode(() -> BumpUseCase.builder()
                .dependencyBumper(dependencyBumper)
                .build()
                .doBump(workspace, new Bump(dependency, latestVersion)))
                .doesNotThrowAnyException();
    }

    private static class TestVersion implements Version {
        @Override
        public String getVersionNumber() {
            return null;
        }

        @Override
        public UpdateType getUpdateType(Version otherVersion) {
            return null;
        }

        @Override
        public int compareTo(Version o) {
            return 0;
        }
    }
}
