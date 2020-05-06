package com.github.autobump.cli.model;

import com.github.tomakehurst.wiremock.WireMockServer;
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
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;

class AutobumpTest {

    private static final String REPO_URL = "http://localhost:8090/repourl";
    private static final String API_URL = "http://localhost:8090/apiurl";
    private static final String GIT_URL = "http://localhost:8091/testuser/testrepo.git";
    Server server;
    private transient WireMockServer wireMockServer;

    private static void populateRepository(Repository repository)
            throws GitAPIException {
        // enable pushing to the sample repository via http
        repository.getConfig().setString("http", null, "receivepack", "true");

        try (Git git = new Git(repository)) {
            File myfile = new File(repository.getDirectory().getParent(), "pom.xml");
            createContent(myfile);
            addAndCommit(git);
        }
    }

    private static void addAndCommit(Git git) throws GitAPIException {
        git.add().addFilepattern("pom.xml").call();
        git.commit().setMessage("Test-Checkin").call();
    }

    private static void createContent(File fileToWriteTo) {
        try (BufferedWriter fw = Files.newBufferedWriter(fileToWriteTo.toPath());
             BufferedReader bufferedReader =
                     Files.newBufferedReader(new File("src/test/resources/pom.xml").toPath())) {
            copyFileContent(fw, bufferedReader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(options().port(8090));
        wireMockServer.start();
        setupStub();
    }

    private void setupStub() {
        wireMockServer.stubFor(get(urlEqualTo("/maven2/test/test/maven-metadata.xml"))
                .willReturn(aResponse().withHeader("Content-Type", "text/xml")
                        .withStatus(200)
                        .withBodyFile("metadata/maven-metadata.xml")));
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void main() {
        String[] args = ("-u glenn.schrooyen@student.kdg.be -p AutoBump2209 -l " + GIT_URL).split(" ");
        int exit = new CommandLine(
                new Autobump(
                        REPO_URL,
                        API_URL))
                .execute(args);
        assertThat(exit).isEqualTo(0);
    }

    private void startServer() throws Exception {
        Repository repository = createNewRepository();

        populateRepository(repository);

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
}
