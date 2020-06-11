package com.github.autobump.bitbucket.model;

import com.github.autobump.core.model.gitproviders.GitProviderUrlHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BitBucketUrlHelperTest {
    private static final String TEST_URL = "https://SchroGlenn@bitbucket.org/grietvermeesch/testmavenproject.git";
    private static final String TEST_NONMATCHING_URL = "https://this_is_a_non-matching_url.com";
    private static final String TEST_PR_URL
            = "https://bitbucket.org/grietvermeesch/simplemultimoduletestproject/pull-requests/21";
    private GitProviderUrlHelper gitProviderUrlHelper;

    @BeforeEach
    void setUp() {
        gitProviderUrlHelper = new BitBucketGitProviderUrlHelper();
    }

    @Test
    void getOwnerName() {
        assertThat(gitProviderUrlHelper.getOwnerName(TEST_URL)).isEqualTo("grietvermeesch");
    }

    @Test
    void GetOwnerName_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() ->
                gitProviderUrlHelper.getOwnerName(TEST_NONMATCHING_URL)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getRepoName() {
        assertThat(gitProviderUrlHelper.getRepoName(TEST_URL)).isEqualTo("testmavenproject");
    }

    @Test
    void GetRepoName_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() ->
                gitProviderUrlHelper.getRepoName(TEST_NONMATCHING_URL)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void GetPullrequestId() {
        assertThat(gitProviderUrlHelper.getPullRequestId(TEST_PR_URL)).isEqualTo(21);
    }

    @Test
    void GetPullRequestIdWithNonMatchingUrl_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> gitProviderUrlHelper.getPullRequestId(TEST_NONMATCHING_URL))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
