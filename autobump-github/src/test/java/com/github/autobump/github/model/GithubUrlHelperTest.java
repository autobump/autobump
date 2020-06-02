package com.github.autobump.github.model;

import com.github.autobump.core.model.ReleaseNotesUrlHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GithubUrlHelperTest {
    private static final String TEST_URL = "https://github.com/grietvermeesch/testmavenproject.git";
    private static final String TEST_NONMATCHING_URL = "https://this_is_a_non-matching_url.com";
    private ReleaseNotesUrlHelper releaseNotesUrlHelper;

    @BeforeEach
    void setUp() {
        releaseNotesUrlHelper = new GithubUrlHelper();
    }

    @Test
    void getOwnerName() {
        assertThat(releaseNotesUrlHelper.getOwnerName(TEST_URL)).isEqualTo("grietvermeesch");
    }

    @Test
    void GetOwnerName_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() ->
                releaseNotesUrlHelper.getOwnerName(TEST_NONMATCHING_URL)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getRepoName() {
        assertThat(releaseNotesUrlHelper.getRepoName(TEST_URL)).isEqualTo("testmavenproject");
    }

    @Test
    void GetRepoName_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() ->
                releaseNotesUrlHelper.getRepoName(TEST_NONMATCHING_URL)).isInstanceOf(IllegalArgumentException.class);
    }
}
