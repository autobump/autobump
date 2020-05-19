package com.github.autobump.core.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
@Getter
public class AutoBumpRebaseResult {
    private final boolean hasConflicts;

}
