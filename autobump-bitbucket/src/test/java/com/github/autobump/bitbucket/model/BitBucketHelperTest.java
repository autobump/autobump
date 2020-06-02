package com.github.autobump.bitbucket.model;

import com.github.autobump.core.model.GitProviderUrlHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BitBucketHelperTest {
    private String url;
    private GitProviderUrlHelper gitProviderUrlHelper;

    @BeforeEach
    void setUp() {
        url = "https://SchroGlenn@bitbucket.org/grietvermeesch/testmavenproject.git";
        gitProviderUrlHelper = new BitBucketGitProviderUrlHelper();
    }


    @Test
    void getOwnerName() {
        assertThat(gitProviderUrlHelper.getOwnerName(url)).isEqualTo("grietvermeesch");
    }

    @Test
    void getRepoName() {
        assertThat(gitProviderUrlHelper.getRepoName(url)).isEqualTo("testmavenproject");
    }
}
