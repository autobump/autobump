package com.github.autobump.maven.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Griet Vermeesch
 * @version 1.0 27/04/2020 15:51
 */
class MavenVersionTest {
    private transient MavenVersion mv1;
    private transient MavenVersion mv2;
    private transient MavenVersion mv3;
    private transient MavenVersion mv4;
    private transient MavenVersion mv5;
    private transient MavenVersion mv6;

    @BeforeEach
    void setUp(){
        mv1 = new MavenVersion("5.7.0-M1");
        mv2 = new MavenVersion("4.0.1");
        mv3 = new MavenVersion("4.0.1");
        mv4 = new MavenVersion("1.2-beta-2");
        mv5 = new MavenVersion("1.2");
        mv6 = new MavenVersion("1.2-alpha-6");
    }

    @Test
    void compareOlderToNewVersion() {
        assertTrue(mv2.compareTo(mv1) < 0);
    }

    @Test
    void compareBetaToNonBeta_shouldBeOlder(){
        assertTrue(mv4.compareTo(mv5) < 0);
    }

    @Test
    void compareNewerToOlderVersion(){
        assertTrue(mv1.compareTo(mv2) > 0);
    }

    @Test
    void compareSimilarVersions(){
        assertSame(0, mv2.compareTo(mv3));
    }

    @Test
    void compareAlphaToBeta_shouldBeOlder(){
        assertTrue(mv6.compareTo(mv4) < 0);
    }

}