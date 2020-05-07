package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.UrlHelper;
import com.github.autobump.core.model.Workspace;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;

class PullRequestUseCaseTest {
    private GitProvider gitProvider;
    private GitClient gitClient;
    private UrlHelper urlHelper;
    private Workspace workspace;
    private Bump bump;
    private URI uri;

    @BeforeEach
    void setUp() {
        gitProvider = Mockito.mock(GitProvider.class);
        
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void doPullRequest() {
    }
}
