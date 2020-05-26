package com.github.autobump.jgit.model;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.jgit.exception.GitException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Path;

import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.MAVENTYPE;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.TESTBRANCHNAME_LONG;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.TESTREPO_URL;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.TEST_PASSWORD;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.TEST_USERNAME;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.TEST_VNUMBER;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.createBumpForTest;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.startServer;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.stopServer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class JGitGitClientTest {

    private JGitGitClient client;

    @BeforeEach
    void setUp() throws Exception {
        client = new JGitGitClient(TEST_USERNAME, TEST_PASSWORD);
        FileRepositoryBuilder.create(new File("src/test/resources/__files"));
    }

    @Test
    void testClone() throws Exception {
        startServer(MAVENTYPE);
        assertThat(client.clone(new URI(TESTREPO_URL))).isNotNull();
        stopServer();
    }

    @Test
    void testWrongUrl_ShouldThrowGitException() {
        assertThatExceptionOfType(GitException.class).isThrownBy(() ->
                new JGitGitClient(TEST_USERNAME, TEST_PASSWORD).clone(new URI("wrong")));
    }

    @Test
    void commitToNewBranch_CheckThatBranchIsAddedAndHasCorrectName() throws Exception {
        startServer(MAVENTYPE);
        Workspace workspace = client.clone(new URI(TESTREPO_URL));
        try (Git git = Git.open(Path.of(workspace.getProjectRoot()).toFile())) {
            Bump bump = createBumpForTest(TEST_VNUMBER);
            client.commitToNewBranch(workspace, bump);
            assertThat(git.branchList().call()).hasSize(2);
            assertThat(String.format("refs/heads/autobump/%s/%s", bump.getGroup(),
                    bump.getUpdatedVersion().getVersionNumber()))
                    .isEqualTo(git.branchList().call().get(0).getName())
            ;
        }
        stopServer();
    }

    @Test
    void commitNewBranchForInvalidWorkspace_shouldThrowUncheckedIOException() {
        Workspace invalidWorkspace = new Workspace("test/test/test");
        Bump bump = createBumpForTest(TEST_VNUMBER);
        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> client.commitToNewBranch(invalidWorkspace, bump));
    }

    @Test
    void commitToNewBranch_shouldThrowGitException() throws Exception {

        class JGitGitClientTester extends JGitGitClient {
            JGitGitClientTester(String username, String password) {
                super(username, password);
            }

            @Override
            public String commitAndPushToBranch(Git git,
                                                Bump bump) throws GitAPIException {
                throw new CanceledException("The call was cancelled");
            }

            @Override
            public String createBranch(Git git, Bump bump) throws GitAPIException {
                throw new CanceledException("The call was cancelled");
            }

        }

        startServer(MAVENTYPE);
        JGitGitClientTester testClient = new JGitGitClientTester(TEST_USERNAME, TEST_PASSWORD);
        Workspace workspace = testClient.clone(new URI(TESTREPO_URL));
        Bump bump = createBumpForTest(TEST_VNUMBER);
        assertThatExceptionOfType(GitException.class)
                .isThrownBy(() -> testClient.commitToNewBranch(workspace, bump));
        stopServer();
    }

    @Test
    void commitToExistingBranch() throws Exception {
        startServer(MAVENTYPE);
        Workspace workspace = client.clone(new URI(TESTREPO_URL));
        try (Git git = Git.open(Path.of(workspace.getProjectRoot()).toFile())) {
            Bump bump = createBumpForTest(TEST_VNUMBER);
            client.commitToNewBranch(workspace, bump);
            client.commitToExistingBranch(workspace, bump, TESTBRANCHNAME_LONG);
            assertThat(TESTBRANCHNAME_LONG)
                    .isEqualTo(git.branchList().call().get(0).getName());
        }
        stopServer();
    }

    @Test
    void commitExistingBranchForInvalidWorkspace_shouldThrowUncheckedIOException() {
        Workspace invalidWorkspace = new Workspace("test/test/test");
        Bump bump = createBumpForTest(TEST_VNUMBER);
        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> client.commitToExistingBranch(invalidWorkspace, bump, TESTBRANCHNAME_LONG));
    }

    @Test
    void commitToExistingBranch_shouldThrowGitException() throws Exception {

        class JGitGitClientTester extends JGitGitClient {
            JGitGitClientTester(String username, String password) {
                super(username, password);
            }

            @Override
            public String commitAndPushToBranch(Git git,
                                                Bump bump) throws GitAPIException {
                throw new CanceledException("The call was cancelled");
            }

            @Override
            public String createBranch(Git git, Bump bump) throws GitAPIException {
                throw new CanceledException("The call was cancelled");
            }
        }

        startServer(MAVENTYPE);
        JGitGitClientTester testClient = new JGitGitClientTester(TEST_USERNAME, TEST_PASSWORD);
        Workspace workspace = testClient.clone(new URI(TESTREPO_URL));
        Bump bump = createBumpForTest(TEST_VNUMBER);
        assertThatExceptionOfType(GitException.class)
                .isThrownBy(() -> testClient.commitToExistingBranch(workspace, bump, TESTBRANCHNAME_LONG));
        stopServer();
    }
}
