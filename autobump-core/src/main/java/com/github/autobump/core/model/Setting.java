package com.github.autobump.core.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public final class Setting {
    @NonNull
    private SettingsType type;
    @NonNull
    private String key;
    @NonNull
    private String value;


    enum SettingsType{
        IGNORE
    }
}
