package com.github.autobump.core.usecases;

import com.github.autobump.core.events.PrClosedEvent;
import com.github.autobump.core.model.domain.Bump;
import com.github.autobump.core.model.domain.Dependency;
import com.github.autobump.core.model.domain.Setting;
import com.github.autobump.core.model.domain.Version;
import com.github.autobump.core.repositories.SettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PullRequestClosedUseCaseTest {
    SettingsRepository settingsRepository;

    @BeforeEach
    void setUp() {
        settingsRepository = new TestSettingsRepo();
    }

    @Test
    void doClose() {
        Dependency dependency1 = Dependency.builder().group("org.springframework").name("spring-core")
                .version(new TestVersion("2")).build();
        Dependency dependency2 = Dependency.builder().group("org.springframework").name("spring-beans")
                .version(new TestVersion("3")).build();
        var dependencySet = Set.of(dependency1, dependency2);
        Bump bump = new Bump(dependencySet, new TestVersion("5.2.5.RELEASE"));
        var event = PrClosedEvent.builder().prName(bump.getTitle()).repoName("test").build();
        var setting = PullRequestClosedUseCase.builder()
                .prClosedEvent(event)
                .settingsRepository(settingsRepository)
                .build()
                .doClose();
        assertThat(setting.size()).isEqualTo(2);
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

    private class TestSettingsRepo implements SettingsRepository{

        @Override
        public Setting saveSetting(Setting setting) {
            return setting;
        }

        @Override
        public List<Setting> saveAllSettings(List<Setting> settings) {
            return settings;
        }

        @Override
        public Setting findSettingForReviewer(String repoName) {
            return null;
        }

        @Override
        public List<Setting> getAllIgnores() {
            return Collections.emptyList();
        }

        @Override
        public void deleteAll() {
        }

        @Override
        public List<Setting> findAllSettingsForDependencies(String repoName) {
            return null;
        }

        @Override
        public Setting getCronSetting(String repoName) {
            return null;
        }

        @Override
        public void removeCronJob(String repoName) {
        }
    }
}
