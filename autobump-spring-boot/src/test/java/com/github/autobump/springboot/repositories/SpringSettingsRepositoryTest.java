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
    public static final String REPOSITORY_NAME = "TestMavenProject";

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
        Setting setting = createSetting1();
        Setting setting2 = createSetting2();
        assertThat(springSettingsRepository.saveAllSettings(List.of(setting, setting2))
                .containsAll(List.of(setting, setting2)));
    }

    @Test
    void findSettings(){
        Setting setting = createSetting1();
        Setting setting2 = createSetting2();
        springSettingsRepository.saveSetting(setting);
        springSettingsRepository.saveSetting(setting2);
        assertThat(springSettingsRepository.findAllSettingsForDependencies(REPOSITORY_NAME).size())
                .isEqualTo(2);
    }

    @Test
    void removeCronJob(){
        Setting cronSetting = new Setting();
        cronSetting.setRepositoryName(REPOSITORY_NAME);
        cronSetting.setKey("cron");
        cronSetting.setValue("true");
        cronSetting.setType(Setting.SettingsType.CRON);
        springSettingsRepository.saveSetting(cronSetting);
        springSettingsRepository.removeCronJob(REPOSITORY_NAME);
        assertThat(springSettingsRepository.findAllSettingsForDependencies(REPOSITORY_NAME).size()).isEqualTo(0);
    }

    private Setting createSetting1() {
        return Setting.builder()
                .key("com.h2database:h2:1.4.200")
                .value("Minor")
                .type(Setting.SettingsType.IGNORE)
                .repositoryName(REPOSITORY_NAME)
                .build();
    }

    private Setting createSetting2() {
        return Setting.builder()
                .key("com.h2database:h2:1.4.199")
                .value("Major")
                .repositoryName(REPOSITORY_NAME)
                .type(Setting.SettingsType.IGNORE)
                .build();
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
