package com.github.autobump.springboot.repositories;

import com.github.autobump.core.model.Setting;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles("test")
class SpringSettingsRepositoryTest {

    private final SpringSettingsRepository springSettingsRepository;

    SpringSettingsRepositoryTest(SpringSettingsRepository springSettingsRepository) {
        this.springSettingsRepository = springSettingsRepository;
    }

    @Test
    void saveSetting() {
        var setting = Setting.builder()
                .key("com.h2database:h2:1.4.200")
                .value("Minor")
                .type(Setting.SettingsType.IGNORE)
                .build();
        assertThat(springSettingsRepository.saveSetting(setting)).isEqualToComparingFieldByField(setting);
    }

    @Test
    void saveAllSettings() {
        var setting = Setting.builder()
                .key("com.h2database:h2:1.4.200")
                .value("Minor")
                .type(Setting.SettingsType.IGNORE)
                .build();
        var setting2 = Setting.builder()
                .key("com.h2database:h2:1.4.199")
                .value("Major")
                .type(Setting.SettingsType.IGNORE)
                .build();
        assertThat(springSettingsRepository.saveAllSettings(List.of(setting, setting2))
                .containsAll(List.of(setting, setting2)));
    }
}
