package com.github.autobump.core.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlHelperTest {
    private static final String TEST_URL = "https://github.com/grietvermeesch/testmavenproject.git";
    private static final String TEST_NONMATCHING_URL = "https://this_is_a_non-matching_url.com";
    private UrlHelperTestImpl urlHelper;

    @BeforeEach
    void setUp() {
        urlHelper = new UrlHelperTestImpl();
    }

    @Test
    void getOwnerName() {
        assertThat(urlHelper.getOwnerName(TEST_URL)).isEqualTo("grietvermeesch");
    }

    @Test
    void GetOwnerName_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() ->
                urlHelper.getOwnerName(TEST_NONMATCHING_URL)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getRepoName() {
        assertThat(urlHelper.getRepoName(TEST_URL)).isEqualTo("testmavenproject");
    }

    @Test
    void GetRepoName_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() ->
                urlHelper.getRepoName(TEST_NONMATCHING_URL)).isInstanceOf(IllegalArgumentException.class);
    }

    class UrlHelperTestImpl extends UrlHelper{
    }
}
