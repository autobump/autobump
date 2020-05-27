package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;

@ExtendWith(MockitoExtension.class)
class PushUseCaseTest {
    Bump bump;

    @Mock
    GitClient gitClient;

    @InjectMocks
    PushUseCase pushUseCase;

    @BeforeEach
    void setUp(){
        Dependency d = Dependency.builder().group("").name("").version(new TestVersion()).build();
        bump = new Bump(d, new TestVersion());
        pushUseCase = PushUseCase.builder().gitClient(gitClient).build();
    }

    @Test
    void doPush() {
        assertThatCode(() -> pushUseCase
                .doPush(new Workspace(""), bump,"branchName"))
                .doesNotThrowAnyException();
    }

    private static class TestVersion implements Version {
        private final String version;

        TestVersion() {
            this.version = "1.0.0";
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
            if (this.getVersionNumber().equals("bla")) {
                return -1;
            }
            return 1;
        }
    }
}
