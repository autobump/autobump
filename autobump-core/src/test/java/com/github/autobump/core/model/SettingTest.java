package com.github.autobump.core.model;

import org.junit.jupiter.api.Test;

import static com.github.autobump.core.model.Setting.SettingsType.IGNORE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class SettingTest {

    @Test
    void testEnumExists(){
        assertThat(IGNORE).isNotNull();
    }

    @Test
    void testConstructort(){

        assertThatCode(() -> new Setting(null, null, null, null)).isInstanceOf(NullPointerException.class);
        assertThatCode(() -> new Setting(IGNORE, null, null, null)).isInstanceOf(NullPointerException.class);
        assertThatCode(() -> new Setting(IGNORE, "test", null, null)).isInstanceOf(NullPointerException.class);
        assertThatCode(() -> new Setting(IGNORE, "test", "test", null)).isInstanceOf(NullPointerException.class);
    }

}
