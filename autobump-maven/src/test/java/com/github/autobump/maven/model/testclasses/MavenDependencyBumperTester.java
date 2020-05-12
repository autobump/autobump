package com.github.autobump.maven.model.testclasses;

import com.github.autobump.maven.model.MavenDependencyBumper;
import org.apache.maven.model.io.xpp3.MavenXpp3ReaderEx;

import java.io.IOException;

public class MavenDependencyBumperTester extends MavenDependencyBumper {
    MavenXpp3ReaderEx mavenXpp3ReaderEx = new MavenXpp3ReaderEx();

    @Override
    public void updateDependency(MavenUpdate update) throws IOException {
        throw new IOException();
    }
}
