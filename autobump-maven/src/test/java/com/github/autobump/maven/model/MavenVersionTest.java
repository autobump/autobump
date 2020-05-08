package com.github.autobump.maven.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.autobump.core.model.Version.UpdateType.INCREMENTAL;
import static com.github.autobump.core.model.Version.UpdateType.MAJOR;
import static com.github.autobump.core.model.Version.UpdateType.MINOR;
import static com.github.autobump.core.model.Version.UpdateType.NONE;
import static org.assertj.core.api.Assertions.assertThat;


class MavenVersionTest {
    private  MavenVersion mv1;
    private  MavenVersion mv2;
    private  MavenVersion mv3;
    private  MavenVersion mv4;
    private  MavenVersion mv5;
    private  MavenVersion mv6;
    private  MavenVersion mv7;
    private  MavenVersion mv8;


    @BeforeEach
    void setUp(){
        mv1 = new MavenVersion("5.7.0-M1");
        mv2 = new MavenVersion("4.0.1");
        mv3 = new MavenVersion("4.0.1");
        mv4 = new MavenVersion("1.2-beta-2");
        mv5 = new MavenVersion("1.2");
        mv6 = new MavenVersion("1.2-alpha-6");
        mv7 = new MavenVersion("1.3");
        mv8 = new MavenVersion("4.0.2");
    }

    @Test
    void compareOlderToNewVersion() {
        assertThat(mv2.compareTo(mv1)).isGreaterThan(0);
    }

    @Test
    void compareBetaToNonBeta_shouldBeOlder(){
        assertThat(mv4.compareTo(mv5)).isGreaterThan(0);
    }

    @Test
    void compareNewerToOlderVersion(){
        assertThat(mv1.compareTo(mv2)).isLessThan(0);
    }

    @Test
    void compareSimilarVersions(){
        assertThat(mv2.compareTo(mv3)).isEqualTo(0);
    }

    @Test
    void compareAlphaToBeta_shouldBeOlder(){
        assertThat(mv6.compareTo(mv4)).isGreaterThan(0);
    }

    @Test
    void updateType_Major() {
        assertThat(mv2.getUpdateType(mv1)).isEqualTo(MAJOR);
    }

    @Test
    void updateType_Minor(){
        assertThat(mv5.getUpdateType(mv7)).isEqualTo(MINOR);
    }

    @Test
    void updateType_Incremental(){
        assertThat(mv2.getUpdateType(mv8)).isEqualTo(INCREMENTAL);
    }

    @Test
    void updateType_None(){
        assertThat(mv2.getUpdateType(mv3)).isEqualTo(NONE);
    }

    @Test
    void updateType_betaToMinor(){
        assertThat(mv4.getUpdateType(mv5)).isEqualTo(MINOR);
    }
}
