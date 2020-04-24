package com.github.autobump.core.model;

import java.net.URI;

public interface GitClient {
    Workspace clone(URI uri);
}
