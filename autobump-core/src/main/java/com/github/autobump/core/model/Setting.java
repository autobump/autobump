package com.github.autobump.core.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@EqualsAndHashCode(of = "key")
@Entity
public final class Setting {
    private SettingsType type;
    @Id
    private String key;
    private String value;

    protected Setting(){
    }

    @Builder
    public Setting(@NonNull SettingsType type, @NonNull String key, @NonNull String value){
        this.type = type;
        this.key = key;
        this.value = value;
    }

    public enum SettingsType{
        IGNORE
    }
}
