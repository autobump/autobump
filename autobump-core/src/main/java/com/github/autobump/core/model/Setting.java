package com.github.autobump.core.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Builder
@EqualsAndHashCode(of = "key")
@Entity
public final class Setting {
    @NonNull
    private SettingsType type;
    @NonNull
    @Id
    private String key;
    @NonNull
    private String value;

    protected Setting(){
    }

    public enum SettingsType{
        IGNORE
    }
}
