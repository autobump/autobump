package com.github.autobump.core.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class AutoBumpRebaseResult {
    private final boolean hasConflicts;

}
