package com.github.autobump.core.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SettingTest {

    @Test
    void testEnumExists(){
        assertThat(Setting.SettingsType.IGNORE).isNotNull();
    }

}
