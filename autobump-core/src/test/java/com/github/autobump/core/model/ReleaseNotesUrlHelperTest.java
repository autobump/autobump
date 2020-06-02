package com.github.autobump.core.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReleaseNotesUrlHelperTest {

    private ReleaseNotesUrlHelper releaseNotesUrlHelper;

    @BeforeEach
    void setUp() {
        releaseNotesUrlHelper = new ReleaseNotesUrlHelperImpl();
    }

    @Test
    void gitProviderUrlHelper_isUrlHelper(){
        assertThat(releaseNotesUrlHelper).isInstanceOf(UrlHelper.class);
    }

    @Test
    void gitProviderUrlHelper_isGitProviderUrlHelper(){
        assertThat(releaseNotesUrlHelper).isInstanceOf(ReleaseNotesUrlHelper.class);
    }

    class ReleaseNotesUrlHelperImpl extends ReleaseNotesUrlHelper{
    }

}
