package com.github.autobump.core.model;

public interface IgnoreRepository {
    boolean isIgnored(Dependency dependency, Version version);
}
