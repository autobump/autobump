package com.github.autobump.springboot.repositories;

import com.github.autobump.core.model.Setting;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@EnableJpaRepositories
class SpringSettingsRepositoryTest {
    @Autowired
    private JpaRepository jpaRepository;

    @Test
    void saveSetting() {
        var setting = Setting.builder()
                .key("com.h2database:h2:1.4.200")
                .value("Minor")
                .type(Setting.SettingsType.IGNORE)
                .build();
        assertThat(jpaRepository.save(setting)).isEqualToComparingFieldByField(setting);
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
        assertThat(jpaRepository.saveAll(List.of(setting, setting2)).containsAll(List.of(setting, setting2)));
    }
}
