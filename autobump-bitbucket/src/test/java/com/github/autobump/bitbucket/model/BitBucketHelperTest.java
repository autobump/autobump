package com.github.autobump.bitbucket.model;

import com.github.autobump.core.model.UrlHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BitBucketHelperTest {
    private String url;
    private UrlHelper urlHelper;

    @BeforeEach
    void setUp() {
        url = "https://SchroGlenn@bitbucket.org/grietvermeesch/testmavenproject.git";
        urlHelper = new BitBucketUrlHelper();
    }


    @Test
    void getOwnerName() {
        assertThat(urlHelper.getOwnerName(url)).isEqualTo("grietvermeesch");
    }

    @Test
    void getRepoName() {
        assertThat(urlHelper.getRepoName(url)).isEqualTo("testmavenproject");
    }
}
