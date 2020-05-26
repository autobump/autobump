package com.github.autobump.core.model.events;

import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class PrClosedEventTest {
    private PrClosedEvent prClosedEvent;

    @BeforeEach
    void setUp() {
        prClosedEvent = PrClosedEvent.builder()
                .repoName("test")
                .prName("Bumped org.hibernate:hibernate-core:5.0.0 to version: 6.0.0.Alpha5")
                .build();
    }

    @Test
    void getBump() {
        var bump = prClosedEvent.getBump();
        for (Dependency dependency : bump.getDependencies()) {
            assertThat(dependency.getName()).isEqualTo("hibernate-core");
            assertThat(dependency.getGroup()).isEqualTo("org.hibernate");
            assertThat(dependency.getVersion().getVersionNumber()).isEqualTo("5.0.0");
        }
        assertThat(bump.getUpdatedVersion().getVersionNumber()).isEqualTo("6.0.0.Alpha5");
    }

    @Test
    void testIllegalArgument(){
        prClosedEvent = PrClosedEvent.builder().prName("hey").repoName("test").build();
        assertThatCode(() -> prClosedEvent.getBump()).isInstanceOf(IllegalArgumentException.class);
    }

    static class TestVersion implements Version {
        private String versionNumber;

        public TestVersion(String versionNumber) {
            this.versionNumber = versionNumber;
        }

        @Override
        public String getVersionNumber() {
            return versionNumber;
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
