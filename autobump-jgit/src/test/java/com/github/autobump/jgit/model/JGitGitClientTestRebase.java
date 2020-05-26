package com.github.autobump.jgit.model;

import com.github.autobump.core.model.AutoBumpRebaseResult;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.jgit.exception.GitException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Path;

import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.MAVENTYPE;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.TESTBRANCHNAME_LONG;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.TESTBRANCHNAME_SHORT;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.TESTREPO_URL;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.TEST_PASSWORD;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.TEST_USERNAME;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.TEST_VNUMBER;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.createBumpForTest;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.makeChangesToPom;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.startServer;
import static com.github.autobump.jgit.helpers.AutobumpJGitHelper.stopServer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class JGitGitClientTestRebase {

    private JGitGitClient client;
    private Workspace workspace;

    @BeforeEach
    void setUp() throws Exception {
        client = new JGitGitClient(TEST_USERNAME, TEST_PASSWORD);
        FileRepositoryBuilder.create(new File("src/test/resources/__files"));
        startServer(MAVENTYPE);
        workspace = client.clone(new URI(TESTREPO_URL));
    }

    @AfterEach
    void tearDown() throws Exception {
        stopServer();
    }

    @Test
    void rebaseBranchFromMaster_HasConflicts() throws Exception {
        try (Git git = Git.open(Path.of(workspace.getProjectRoot()).toFile())) {
            setupInitialBump(workspace, git);
            setupConflictingBumpInRemote(workspace);
            assertThat(client.rebaseBranchFromMaster(workspace, TESTBRANCHNAME_SHORT).isConflicted()).isTrue();
        }
    }

    @Test
    void rebaseBranchFromMaster_HasConflictsNoLocalBranch() throws Exception {
        try (Git git = Git.open(Path.of(workspace.getProjectRoot()).toFile())) {
            setupInitialBump(workspace, git);
            setupConflictingBumpInRemote(workspace);
            git.branchDelete().setBranchNames(TESTBRANCHNAME_LONG).setForce(true).call();
            assertThat(client.rebaseBranchFromMaster(workspace, TESTBRANCHNAME_SHORT).isConflicted()).isTrue();
        }
    }

    @Test
    void rebaseBranchFromMaster_HasNoConflicts() throws Exception {
        try (Git git = Git.open(Path.of(workspace.getProjectRoot()).toFile())) {
            setupInitialBump(workspace, git);
            git.branchDelete().setBranchNames(TESTBRANCHNAME_LONG).setForce(true).call();
            assertThat(client.rebaseBranchFromMaster(workspace, TESTBRANCHNAME_SHORT).isConflicted()).isFalse();
        }
    }

    @Test
    void rebaseBranchFromMaster_shouldThrowGitException() throws Exception {

        class JGitGitClientTester extends JGitGitClient {
            JGitGitClientTester(String username, String password) {
                super(username, password);
            }

            @Override
            public AutoBumpRebaseResult getAutoBumpRebaseResult(String branchName, Git git)
                    throws GitAPIException, IOException {
                throw new CanceledException("The call was cancelled");
            }
        }

        JGitGitClientTester testClient = new JGitGitClientTester(TEST_USERNAME, TEST_PASSWORD);
        workspace = testClient.clone(new URI(TESTREPO_URL));
        assertThatExceptionOfType(GitException.class)
                .isThrownBy(() -> testClient.rebaseBranchFromMaster(workspace, TESTBRANCHNAME_SHORT));
    }

    @Test
    void rebaseBranchFromMaster_shouldThrowUncheckedIOException() throws Exception {

        class JGitGitClientTester extends JGitGitClient {
            JGitGitClientTester(String username, String password) {
                super(username, password);
            }

            @Override
            public AutoBumpRebaseResult getAutoBumpRebaseResult(String branchName, Git git)
                    throws GitAPIException, IOException {
                throw new IOException();
            }
        }

        JGitGitClientTester testClient = new JGitGitClientTester(TEST_USERNAME, TEST_PASSWORD);
        workspace = testClient.clone(new URI(TESTREPO_URL));
        assertThatExceptionOfType(UncheckedIOException.class)
                .isThrownBy(() -> testClient.rebaseBranchFromMaster(workspace, TESTBRANCHNAME_SHORT));
    }

    private void setupConflictingBumpInRemote(Workspace workspace) throws IOException {
        Bump newerBump = createBumpForTest("3.0.0");
        makeChangesToPom(workspace, "3.0.0");
        client.commitToExistingBranch(workspace, newerBump, "master");
    }

    private void setupInitialBump(Workspace workspace, Git git) throws IOException, GitAPIException {
        Bump bump = createBumpForTest(TEST_VNUMBER);
        makeChangesToPom(workspace, TEST_VNUMBER);
        client.commitToNewBranch(workspace, bump);
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(TEST_USERNAME, TEST_PASSWORD))
                .call();
    }
}
