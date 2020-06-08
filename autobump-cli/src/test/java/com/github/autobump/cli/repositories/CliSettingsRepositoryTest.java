package com.github.autobump.cli.repositories;

import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class CliSettingsRepositoryTest {
    private SettingsRepository settingsRepository;
    private Setting setting1;
    private Setting setting2;

    @BeforeEach
    void setUp() {
        settingsRepository = new CliSettingsRepository();
        setting1 = Setting.builder().
                key("key")
                .repositoryName("repoName1")
                .type(Setting.SettingsType.IGNORE)
                .value("value1")
                .build();
        setting2 = Setting.builder().
                key("key")
                .repositoryName("repoName2")
                .type(Setting.SettingsType.REVIEWER)
                .value("value2")
                .build();
    }

    @Test
    void saveSetting() {
        assertThat(settingsRepository.saveSetting(setting1)).isEqualTo(setting1);
    }

    @Test
    void saveAllSettings() {
        assertThat(settingsRepository.saveAllSettings(List.of(setting1,setting2)))
                .containsExactlyInAnyOrderElementsOf(List.of(setting1,setting2));
    }

    @Test
    void findAllSettingsForDependencies() {
        settingsRepository.saveAllSettings(List.of(setting1,setting2));
        assertThat(settingsRepository.findAllSettingsForDependencies("repoName1")).containsOnly(setting1);
    }

    @Test
    void getCronSetting() {
        assertThat(settingsRepository.getCronSetting("repoName")).isNull();
    }

    @Test
    void removeCronJob() {
        assertThatCode(() -> settingsRepository.removeCronJob("repoName"))
                .doesNotThrowAnyException();
    }

    @Test
    void getAllIgnores() {
        settingsRepository.saveAllSettings(List.of(setting1,setting2));
        assertThat(settingsRepository.getAllIgnores()).containsOnly(setting1);
    }

    @Test
    void deleteAll(){
        settingsRepository.saveAllSettings(List.of(setting1,setting2));
        assertThatCode(() -> settingsRepository.deleteAll()).doesNotThrowAnyException();
    }
}
