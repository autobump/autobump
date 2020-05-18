package com.github.autobump.springboot.repositories;

import com.github.autobump.core.model.Setting;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SpringSettingsRepositoryTest {
    @Autowired
    private SpringSettingsRepository springSettingsRepository;

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
    }
}
