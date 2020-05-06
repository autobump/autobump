package com.github.autobump.bitbucket.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BitBucketHelperTest {
    private String url;


    @BeforeEach
    void setUp() {
        url = "https://SchroGlenn@bitbucket.org/grietvermeesch/testmavenproject.git";
    }

    @Test
    void testCreate() {
        new BitBucketHelper();
    }

    @Test
    void getOwnerName() {
        assertEquals("grietvermeesch", BitBucketHelper.getOwnerName(url));
    }

    @Test
    void getRepoName() {
        assertEquals("testmavenproject", BitBucketHelper.getRepoName(url));
    }
}
