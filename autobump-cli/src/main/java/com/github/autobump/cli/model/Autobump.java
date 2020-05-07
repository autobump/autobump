package com.github.autobump.cli.model;

import com.github.autobump.bitbucket.model.BitBuckeUrltHelper;
import com.github.autobump.bitbucket.model.BitBucketAccount;
import com.github.autobump.bitbucket.model.BitBucketGitProvider;
import com.github.autobump.core.model.AutobumpResult;
import com.github.autobump.core.model.AutobumpUseCase;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.jgit.model.JGitGitClient;
import com.github.autobump.maven.model.MavenDependencyBumper;
import com.github.autobump.maven.model.MavenDependencyResolver;
import com.github.autobump.maven.model.MavenVersionRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.PrintStream;
import java.net.URI;
import java.util.concurrent.Callable;

@Command(
        name = "autobump",
        description = "automatically creates a pull-request for every outdated dependency in a project"
)
public class Autobump implements Callable<Integer> {
    private final DependencyResolver dependencyResolver = new MavenDependencyResolver();
    private final DependencyBumper dependencyBumper = new MavenDependencyBumper();
    private VersionRepository versionRepository;
    private GitClient gitClient;
    private GitProvider gitProvider;
    @Option(names = {"-u", "--username"}, description = "User name for your remote repository", required = true)
    private String username;
    @Option(names = {"-l", "--url"}, paramLabel = "REPOURL", description = "project repository url", required = true)
    private URI url;
    @Option(names = {"-p", "--password"}, description = "Password for your remote repository", required = true)
    private String password;
    @Option(names = {"-r", "--repourl"}, description = "public repositoryUrl for dependency version information",
            defaultValue = "https://repo1.maven.org/maven2")
    private String repositoryUrl;
    @Option(names = {"-a", "--apiurl"}, description = "apiUrl", defaultValue = "https://api.bitbucket.org/2.0")
    private String apiUrl;

    public static void main(String[] args) {
        new CommandLine(new Autobump()).execute(args);
    }

    @Override
    public Integer call() {
        initialize();
        AutobumpResult result = getAutobumpUseCase().execute();
        try (PrintStream out = System.out) {
            out.println("amountBumped: " + result.getNumberOfBumps());
        }
        return 0;
    }

    private AutobumpUseCase getAutobumpUseCase() {
        return AutobumpUseCase.builder()
                .dependencyBumper(dependencyBumper)
                .dependencyResolver(dependencyResolver)
                .gitClient(gitClient)
                .gitProvider(gitProvider)
                .urlHelper(new BitBuckeUrltHelper())
                .uri(url)
                .versionRepository(versionRepository)
                .build();
    }

    private void initialize() {
        versionRepository = new MavenVersionRepository(repositoryUrl);
        gitClient = new JGitGitClient(username, password);
        BitBucketAccount bitBucketAccount = new BitBucketAccount(username, password);
        gitProvider = new BitBucketGitProvider(bitBucketAccount, apiUrl);
    }
}
