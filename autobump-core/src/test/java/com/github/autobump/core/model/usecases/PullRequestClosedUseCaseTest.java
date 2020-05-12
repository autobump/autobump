package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import com.github.autobump.core.model.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PullRequestClosedUseCaseTest {
    SettingsRepository settingsRepository;

    @BeforeEach
    void setUp() {
        makeMocks();
        setUpMocks();
    }

    private void makeMocks() {
        settingsRepository = Mockito.mock(SettingsRepository.class);
    }

    private void setUpMocks() {
        Setting setting1 = Setting.builder()
                .key("org.springframework:spring-beans")
                .value("5.2.5.RELEASE")
                .type(Setting.SettingsType.IGNORE)
                .build();
        Setting setting2 = Setting.builder()
                .key("org.springframework:spring-beans")
                .value("5.2.5.RELEASE")
                .type(Setting.SettingsType.IGNORE)
                .build();
        List<Setting> settingslist = List.of(setting1, setting2);
        Mockito.when(settingsRepository.saveAllSettings(settingslist))
                .thenReturn(settingslist);
    }


    @Test
    void doClose() {
        Dependency dependency1 = Dependency.builder().group("org.springframework").name("derby").build();
        Dependency dependency2 = Dependency.builder().group("org.springframework").name("hibernate").build();
        var dependencySet = Set.of(dependency1, dependency2);
        Bump bump = new Bump(dependencySet, new TestVersion("5.2.5.RELEASE"));
        var setting = PullRequestClosedUseCase.builder()
                .bump(bump)
                .settingsRepository(settingsRepository)
                .build()
                .doClose();
        assertThat(setting).isNotNull();
    }

    private class TestVersion implements Version {
        private final String versionNumber;

        private TestVersion(String versionNumber) {
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
