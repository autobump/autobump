package com.github.autobump.core.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SettingId implements Serializable {
    private static final long serialVersionUID = 2026065648270360672L;

    private String key;
    private String repositoryName;
}
