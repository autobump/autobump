package com.github.autobump.jgit.model;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.jgit.exception.GitException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CanceledException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class JGitGitClientTest {
    static final String MAVENTYPE = "Maven";
    Server server;
    JGitGitClient client;

    @BeforeEach
    void setUp() throws Exception {
        client = new JGitGitClient("test", "test");
        FileRepositoryBuilder.create(new File("src/test/resources/__files"));
    }

    @Test
    void testClone() throws Exception {
        startServer(MAVENTYPE);
        assertThat(client.clone(new URI("http://localhost:8090/TestRepo"))).isNotNull();
        stopServer();
    }

    @Test
    void testWrongUrl_ShouldThrowGitException() {
        assertThatExceptionOfType(GitException.class).isThrownBy(() ->
                new JGitGitClient("test", "test").clone(new URI("wrong")));
    }

    @Test
    void commitToNewBranch_CheckThatBranchIsAddedAndHasCorrectName() throws Exception {
        startServer(MAVENTYPE);
        Workspace workspace = client.clone(new URI("http://localhost:8090/TestRepo"));
        try (Git git = Git.open(Path.of(workspace.getProjectRoot()).toFile())) {
            Bump bump = getBumpForCreationBranch();

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
        Bump bump = getBumpForCreationBranch();
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
        JGitGitClientTester testClient = new JGitGitClientTester("test", "test");
        Workspace workspace = testClient.clone(new URI("http://localhost:8090/TestRepo"));
        Bump bump = getBumpForCreationBranch();
        assertThatExceptionOfType(GitException.class)
                .isThrownBy(() -> testClient.commitToNewBranch(workspace,bump));
        stopServer();
    }

    private Bump getBumpForCreationBranch() {
        Dependency dep = Dependency.builder().group("test").name("test").version(new TestVersion("1.0.0")).build();
        Version version = new Version() {
            @Override
            public int compareTo(Version o) {
                return 0;
            }

            @Override
            public String getVersionNumber() {
                return "2.0.0";
            }

            @Override
            public UpdateType getUpdateType(Version otherVersion) {
                return null;
            }
        };
        return new Bump(dep, version);
    }

    private void stopServer() throws Exception {
        server.stop();
    }

    private void startServer(String dependencyType) throws Exception {
        Repository repository = createNewRepository();

        populateRepository(repository, dependencyType);

        // Create the JGit Servlet which handles the Git protocol
        GitServlet gs = new GitServlet();
        gs.setRepositoryResolver((req, name) -> {
            repository.incrementOpen();
            return repository;
        });

        // start up the Servlet and start serving requests
        server = configureAndStartHttpServer(gs);

        // finally wait for the Server being stopped
    }

    private static Server configureAndStartHttpServer(GitServlet gs) throws Exception {
        Server server = new Server(8090);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        ServletHolder holder = new ServletHolder(gs);

        handler.addServletWithMapping(holder, "/*");

        server.start();
        return server;
    }


    private static void populateRepository(Repository repository,
                                           String dependencyType)
            throws GitAPIException {
        // enable pushing to the sample repository via http
        repository.getConfig().setString("http", null, "receivepack", "true");

        try (Git git = new Git(repository)) {
            if (MAVENTYPE.equals(dependencyType)) {
                File myfile = new File(repository.getDirectory().getParent(), "pom.xml");
                createContent(myfile, dependencyType);
            } else {
                new File(repository.getDirectory().getParent(), "dummy");
            }

            addAndCommit(git);
        }
    }

    private static void addAndCommit(Git git) throws GitAPIException {
        git.add().addFilepattern("pom.xml").call();
        git.commit().setMessage("Test-Checkin").call();
    }

    private static void createContent(File fileToWriteTo, String dependencyType) {
        if (MAVENTYPE.equals(dependencyType)) {
            try (BufferedWriter fw = Files.newBufferedWriter(fileToWriteTo.toPath());
                    BufferedReader bufferedReader =
                            Files.newBufferedReader(new File("src/test/resources/pom.xml").toPath())) {
                copyFileContent(fw, bufferedReader);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private static void copyFileContent(BufferedWriter fw, BufferedReader bufferedReader) throws IOException {
        String s = bufferedReader.readLine();

        while (s != null) {
            fw.write(s);
            fw.flush();
            s = bufferedReader.readLine();
        }
    }

    private static Repository createNewRepository() throws IOException {
        // prepare a new folder
        File localPath = File.createTempFile("TestGitRepository", "");
        if (!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }

        if (!localPath.mkdirs()) {
            throw new IOException("Could not create directory " + localPath);
        }

        // create the directory
        Repository repository = FileRepositoryBuilder.create(new File(localPath, ".git"));
        repository.create();

        return repository;
    }

    class TestVersion implements Version{
        private final String versionNumber;

        TestVersion(String versionNumber) {
            this.versionNumber = versionNumber;
        }

        @Override
        public String getVersionNumber() {
            return versionNumber;
        }

        @Override
        public UpdateType getUpdateType(Version otherVersion) {
            return null;
        }

        @Override
        public int compareTo(Version o) {
            return this.versionNumber.compareTo(o.getVersionNumber());
        }
    }
}
