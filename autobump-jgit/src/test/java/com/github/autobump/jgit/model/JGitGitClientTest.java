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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JGitGitClientTest {
    static final String MAVENTYPE = "Maven";
    Server server;
    JGitGitClient client;

    @BeforeEach
    void setUp() throws Exception {
        client = new JGitGitClient();
        FileRepositoryBuilder.create(new File("src/test/resources/__files"));
    }


    @Test
    void testClone() throws Exception {
        startServer(MAVENTYPE);

        assertNotNull(client.clone(new URI("http://localhost:8080/TestRepo")));
        stopServer();
    }

    @Test
    void testWrongUrl() {
        assertThrows(GitException.class, () ->
                new JGitGitClient().clone(new URI("wrong")));
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
        Server server = new Server(8080);

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

    @Test
    void commitToNewBranch() throws Exception {
        startServer(MAVENTYPE);
        Workspace workspace = client.clone(new URI("http://localhost:8080/TestRepo"));
        try(Git git = Git.open(Path.of(workspace.getProjectRoot()).toFile())){
            Bump bump = getBumpForCreationBranch();

            client.CommitToNewBranch(workspace, bump);

            assertEquals(2, git.branchList().call().size());
            assertEquals(String.format("refs/heads/autobump/%s/%s/%s",
                    bump.getDependency().getGroup(),
                    bump.getDependency().getName(),
                    bump.getUpdatedVersion().getVersionNumber()), git.branchList().call().get(0).getName());
        }
        stopServer();
    }

    @Test
    void commitNewBranchForInvalidWorkspace_shouldThrowUncheckedIOException() {
        Workspace invalidWorkspace = new Workspace("test/test/test");
        Bump bump = getBumpForCreationBranch();
        assertThrows(UncheckedIOException.class, () -> client.CommitToNewBranch(invalidWorkspace, bump));
    }

    @Test
    void commitToNewBranch_shouldThrowGitException() throws Exception {
        startServer(MAVENTYPE);
        JGitGitClientTester testClient = new JGitGitClientTester();
        Workspace workspace = testClient.clone(new URI("http://localhost:8080/TestRepo"));
        Bump bump = getBumpForCreationBranch();
        assertThrows(GitException.class, () -> testClient.CommitToNewBranch(workspace, bump));
        stopServer();
    }

    private Bump getBumpForCreationBranch() {
        Dependency dep = Dependency.builder().group("test").name("test").version("1.0.0").build();
        Version version = new Version() {
            @Override
            public int compareTo(Version o) {
                return 0;
            }

            @Override
            public String getVersionNumber() {
                return "2.0.0";
            }
        };
        return new Bump(dep, version);
    }

    static class JGitGitClientTester extends JGitGitClient {

        @Override
        public void createBranch(Bump bump, Git git) throws CanceledException {
            throw new CanceledException("The call was cancelled");
        }

        @Override
        public void commitAndPushToNewBranch(Bump bump, Git git) throws CanceledException {
            throw new CanceledException("The call was cancelled");
        }
    }
}
