package com.github.autobump.maven.model.testclasses;

import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Version;
import com.github.autobump.maven.model.MavenDependencyBumper;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.io.xpp3.MavenXpp3ReaderEx;

import java.io.IOException;

public class MavenDependencyBumperTester extends MavenDependencyBumper {
    MavenXpp3ReaderEx mavenXpp3ReaderEx = new MavenXpp3ReaderEx();

    @Override
    public void updateDependency(InputLocation versionLocation, Dependency dependency, Version version) throws IOException {
        throw new IOException();
    }
}
