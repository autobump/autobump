package com.github.autobump.core.model;

import com.github.autobump.core.model.gitproviders.GitProviderUrlHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GitProviderUrlHelperTest {

    private GitProviderUrlHelper gitProviderUrlHelper;

    @BeforeEach
    void setUp() {
        gitProviderUrlHelper = new GitProviderUrlHelperImpl();
    }

    @Test
    void gitProviderUrlHelper_isUrlHelper(){
        assertThat(gitProviderUrlHelper).isInstanceOf(UrlHelper.class);
    }

    @Test
    void gitProviderUrlHelper_isGitProviderUrlHelper(){
        assertThat(gitProviderUrlHelper).isInstanceOf(GitProviderUrlHelper.class);
    }

    class GitProviderUrlHelperImpl extends GitProviderUrlHelper{
        @Override
        public int getPullRequestId(String pullRequestUrl) {
            return 0;
        }
    }
}
