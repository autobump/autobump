package com.github.autobump.core.model;

import java.util.Set;

public interface PluginResolver {
    Set<Plugin> resolve(Workspace workspace);
}
