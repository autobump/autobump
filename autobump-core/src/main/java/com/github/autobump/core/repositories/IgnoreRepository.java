package com.github.autobump.core.repositories;

import com.github.autobump.core.model.domain.Dependency;
import com.github.autobump.core.model.domain.Version;

public interface IgnoreRepository {
    boolean isIgnored(Dependency dependency, Version version);
}
