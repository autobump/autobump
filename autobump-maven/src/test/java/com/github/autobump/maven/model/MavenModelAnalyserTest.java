package com.github.autobump.maven.model;

import com.github.autobump.core.model.Workspace;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;

import static org.assertj.core.api.Assertions.assertThat;

class MavenModelAnalyserTest {
    transient MavenModelAnalyser mavenModelAnalyser;

    @BeforeEach
    void setUp() {
        mavenModelAnalyser = new MavenModelAnalyser();
    }

    @Test
    void getModel() throws IOException, XmlPullParserException {
        Workspace workspace = new Workspace("src/test/resources/project_root");
        try(Reader dependencyDocument = workspace.getDependencyDocument("pom.xml")){
            assertThat(new MavenXpp3Reader().read(dependencyDocument).getDependencies())
                    .hasSameSizeAs(mavenModelAnalyser.getModel(workspace).getDependencies());
        }
    }

    @Test
    void getVersionFromProperties() throws IOException, XmlPullParserException {
        Workspace ws = new Workspace("src/test/resources/project_root_support_properties");
        String version = mavenModelAnalyser.getVersionFromProperties(
                new MavenXpp3Reader().read(ws.getDependencyDocument("pom.xml")),
                "${org.apache.derby.version}");
        assertThat(version).isEqualTo("10.15.2.0");
    }
}
