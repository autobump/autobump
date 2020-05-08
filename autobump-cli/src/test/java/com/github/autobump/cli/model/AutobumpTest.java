package com.github.autobump.cli.model;

import com.github.autobump.core.model.AutobumpResult;
import com.github.autobump.core.model.usecases.AutobumpUseCase;
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
import org.mockito.Mockito;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

class AutobumpTest {

    private static final String REPO_URL = "http://localhost:8090/repourl";
    private static final String API_URL = "http://localhost:8090/apiurl";
    private static final String GIT_URL = "http://localhost:8091/testOwner/testRepoName.git";
    private static final String TEST_OWNER = "testOwner";
    private static final String TEST_REPO_NAME = "testRepoName";
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

    private static Server configureAndStartHttpServer(GitServlet gs) throws Exception {
        Server server = new Server(8091);

        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);

        ServletHolder holder = new ServletHolder(gs);

        handler.addServletWithMapping(holder, "/*");

        server.start();
        return server;
    }

    @BeforeEach
    void setUp() throws Exception {
        wireMockServer = new WireMockServer(options().port(8090));
        wireMockServer.start();
        setupStub();
        startServer();
    }

    private void setupStub() {
        wireMockServer.stubFor(get(urlEqualTo("/repourl/org/apache/derby/derby/maven-metadata.xml"))
                .willReturn(aResponse().withHeader("Content-Type", "text/xml")
                        .withStatus(200)
                        .withBodyFile("metadata/maven-metadata.xml")));
        wireMockServer.stubFor(post(
                urlEqualTo(String.format("/apiurl/repositories/%s/%s/pullrequests", TEST_OWNER, TEST_REPO_NAME)))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBodyFile("succes_response.json"))
                .withBasicAuth("glenn.schrooyen@student.kdg.be", "AutoBump2209"));
    }

    @AfterEach
    void tearDown() throws Exception {
        wireMockServer.stop();
        server.stop();
    }

    @Test
    void main_showsHelpWhenNoParameters() {
        CommandLine cmd = new CommandLine(new Autobump());
        StringWriter sw = new StringWriter();
        cmd.setErr(new PrintWriter(sw));
        cmd.execute();
        assertThat(sw.toString())
                .startsWith("Missing required options")
                .contains("--url")
                .contains("--username")
                .contains("--password");
    }

    @Test
    void main_SuccessfullyShowsResult(){
        String[] args = ("-u glenn.schrooyen@student.kdg.be -p AutoBump2209 -l " + GIT_URL).split(" ");
        CommandLine cmd = new CommandLine(new TestAutoBump());
        cmd.execute(args);
        if (cmd.getExecutionResult() instanceof AutobumpResult) {
            assertThat(((AutobumpResult) cmd.getExecutionResult()).getNumberOfBumps())
                    .isEqualTo(5);
        }
        else {
            fail("bad returntype");
        }
    }

    @Test
    void main_integrationTest(){
        String[] args = String.format("-u glenn.schrooyen@student.kdg.be -p AutoBump2209 -l %s -r %s -a %s",
                GIT_URL, REPO_URL, API_URL).split(" ");
        Autobump.main(args);
        // TODO: 7/05/2020 test if the branch is made on the git repo
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

    static class TestAutoBump extends Autobump {
        @Override
        public AutobumpUseCase getAutobumpUseCase() {
            AutobumpUseCase mocked = Mockito.mock(AutobumpUseCase.class);
            when(mocked.doAutoBump()).thenReturn(new AutobumpResult(5));
            return mocked;
        }
    }
}
