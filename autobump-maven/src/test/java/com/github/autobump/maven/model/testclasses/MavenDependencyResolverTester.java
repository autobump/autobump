package com.github.autobump.maven.model.testclasses;

import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.maven.model.MavenDependencyResolver;

import java.io.IOException;
import java.util.Set;

public class MavenDependencyResolverTester extends MavenDependencyResolver {

    @Override
    public Set<Dependency> resolveModules(Workspace workspace) throws IOException {
        throw new IOException();
    }
}
