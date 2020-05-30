package com.github.autobump.bitbucket.model;

import com.github.autobump.core.model.UrlHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BitBucketUrlHelperTest {
    private String url;
    private String nonMatchingUrl;
    private String prUrl;
    private UrlHelper urlHelper;

    @BeforeEach
    void setUp() {
        url = "https://SchroGlenn@bitbucket.org/grietvermeesch/testmavenproject.git";
        nonMatchingUrl = "https://this_is_a_non-matching_url.com";
        prUrl = "https://bitbucket.org/grietvermeesch/simplemultimoduletestproject/pull-requests/21";
        urlHelper = new BitBucketUrlHelper();
    }

    @Test
    void getOwnerName() {
        assertThat(urlHelper.getOwnerName(url)).isEqualTo("grietvermeesch");
    }

    @Test
    void GetOwnerName_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> urlHelper.getOwnerName(nonMatchingUrl)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getRepoName() {
        assertThat(urlHelper.getRepoName(url)).isEqualTo("testmavenproject");
    }

    @Test
    void GetRepoName_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> urlHelper.getRepoName(nonMatchingUrl)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void GetPullrequestId() {
        assertThat(urlHelper.getPullRequestId(prUrl)).isEqualTo(21);
    }

    @Test
    void GetPullRequestIdWithNonMatchingUrl_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> urlHelper.getPullRequestId(nonMatchingUrl))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
