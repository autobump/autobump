package com.github.autobump.jgit.helpers;

import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.Workspace;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.http.server.GitServlet;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static org.eclipse.jgit.lib.Constants.R_HEADS;

public class AutobumpJGitHelper {
    public static final String MAVENTYPE = "Maven";
    public static final String TESTBRANCHNAME_SHORT = "autobump/test/2.0.0";
    public static final String TESTBRANCHNAME_LONG = R_HEADS + TESTBRANCHNAME_SHORT;
    public static final String TESTREPO_URL = "http://localhost:8090/TestRepo";
    public static final String TEST_USERNAME = "test";
    public static final String TEST_PASSWORD = "test";
    public static final String TEST_VNUMBER = "2.0.0";
    public static final String WORKSPACEROOT_INVALID = "test/test/test";
    public static final String TEST_EXCEPTION_MESSAGE = "the test operation failed";
    public static Server server;

    public static void startServer(String dependencyType) throws IOException, GitAPIException {
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

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public static void stopServer() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new GitTestServerException("Error stopping Git Test Server", e);
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private static Server configureAndStartHttpServer(GitServlet gs) {
        server = new Server(8090);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        ServletHolder holder = new ServletHolder(gs);
        handler.addServletWithMapping(holder, "/*");
        try {
            server.start();
        } catch (Exception e) {
            throw new GitTestServerException("Error starting Git Test Server", e);
        }
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
            try (BufferedWriter fw = Files.newBufferedWriter(fileToWriteTo.toPath()); BufferedReader bufferedReader =
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

    public static void makeChangesToPom(Workspace workspace, String text) throws IOException {
        Path file = Paths.get(workspace.getProjectRoot() + File.separator + "pom.xml");
        List<String> out = Files.readAllLines(file);
        out.set(0, out.get(0).replace("10.15.2.0", text));
        Files.write(file, out, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static Bump createBumpForTest(String versionNumber) {
        Version version = new TestVersion(versionNumber);
        Dependency dep = Dependency.builder().group("test").name("test").version(version).build();
        return new Bump(dep, version);
    }

    static class TestVersion implements Version {
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
            return 1;
        }
    }

    private static class GitTestServerException extends RuntimeException {
        private static final long serialVersionUID = 7183617712122897493L;
        GitTestServerException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
