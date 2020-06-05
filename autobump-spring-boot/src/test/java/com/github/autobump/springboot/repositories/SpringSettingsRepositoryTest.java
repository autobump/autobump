package com.github.autobump.springboot.repositories;

import com.github.autobump.core.model.Setting;
import com.github.autobump.core.model.SettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class SpringSettingsRepositoryTest {
    @Autowired
    private SettingsRepository springSettingsRepository;

    @Test
    void saveSetting() {
        var setting = Setting.builder()
                .key("com.h2database:h2:1.4.200")
                .value("Minor")
                .type(Setting.SettingsType.IGNORE)
                .repositoryName("test")
                .build();
        assertThat(springSettingsRepository.saveSetting(setting)).isEqualToComparingFieldByField(setting);
    }

    @Test
    void saveAllSettings() {
        var setting = Setting.builder()
                .key("com.h2database:h2:1.4.200")
                .value("Minor")
                .type(Setting.SettingsType.IGNORE)
                .repositoryName("test")
                .build();
        var setting2 = Setting.builder()
                .key("com.h2database:h2:1.4.199")
                .value("Major")
                .repositoryName("test")
                .type(Setting.SettingsType.IGNORE)
                .build();
        assertThat(springSettingsRepository.saveAllSettings(List.of(setting, setting2))
                .containsAll(List.of(setting, setting2)));
    }

    @Test
    void testgetIgnores() {
        var setting = Setting.builder()
                .key("com.h2database:h2:1.4.200")
                .value("Minor")
                .type(Setting.SettingsType.IGNORE)
                .repositoryName("test")
                .build();
        springSettingsRepository.saveSetting(setting);
        assertThat(springSettingsRepository.getAllIgnores()).contains(setting);
    }
}
