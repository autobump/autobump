package com.github.autobump.cli.model;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

class AutobumpTest {
    static final String MAVENTYPE = "Maven";
    Server server;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
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

}