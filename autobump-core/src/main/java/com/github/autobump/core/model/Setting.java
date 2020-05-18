package com.github.autobump.core.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Data
@EqualsAndHashCode(of = "key")
@Entity
@IdClass(SettingId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class Setting {
    private SettingsType type;
    @Id
    private String repositoryName;
    @Id
    @Column(name = "`key`")
    private String key;
    private String value;

    @Builder
    public Setting(@NonNull SettingsType type, @NonNull String key, @NonNull String value,
                   @NonNull String repositoryName){
        this.type = type;
        this.key = key;
        this.value = value;
        this.repositoryName = repositoryName;
    }

    public enum SettingsType{
        IGNORE
    }
}
