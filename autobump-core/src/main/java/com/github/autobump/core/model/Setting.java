package com.github.autobump.core.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@Builder
@EqualsAndHashCode(of = "key")
public final class Setting {
    @NonNull
    private SettingsType type;
    @NonNull
    private String key;
    @NonNull
    private String value;


    public enum SettingsType{
        IGNORE
    }
}
