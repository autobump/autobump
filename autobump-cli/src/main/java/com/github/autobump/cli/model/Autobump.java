package com.github.autobump.cli.model;

import com.github.autobump.bitbucket.model.BitBucketAccount;
import com.github.autobump.bitbucket.model.BitBucketGitProvider;
import com.github.autobump.bitbucket.model.BitBucketUrlHelper;
import com.github.autobump.core.model.AutobumpResult;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.core.model.usecases.AutobumpUseCase;
import com.github.autobump.jgit.model.JGitGitClient;
import com.github.autobump.maven.model.MavenDependencyBumper;
import com.github.autobump.maven.model.MavenDependencyResolver;
import com.github.autobump.maven.model.MavenVersionRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(
        name = "autobump",
        description = "automatically creates a pull-request for every outdated dependency in a project"
)
public class Autobump implements Callable<AutobumpResult> {
    @CommandLine.Spec
    private static CommandSpec spec;
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
    @Option(names = {"-r", "--repourl"}, description = "Public repositoryUrl for dependency version information",
            defaultValue = "https://repo1.maven.org/maven2")
    private String repositoryUrl;
    @Option(names = {"-a", "--apiurl"}, description = "apiUrl", defaultValue = "https://api.bitbucket.org/2.0")
    private String apiUrl;
    @Option(names = {"-i", "--ignored"}, description = "Dependencies to ignore for updates including update" +
            " type separated by comma", split = ",")
    private Map<String,String> ignoreDependencies;

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Autobump());
        cmd.execute(args);
        AutobumpResult result = cmd.getExecutionResult();
        spec.commandLine().getOut().println("amountBumped: " + result.getNumberOfBumps());
    }

    @Override
    public AutobumpResult call() {
        initialize();
        return getAutobumpUseCase().doAutoBump();
    }

    private void initialize() {
        versionRepository = new MavenVersionRepository(repositoryUrl);
        gitClient = new JGitGitClient(username, password);
        BitBucketAccount bitBucketAccount = new BitBucketAccount(username, password);
        gitProvider = new BitBucketGitProvider(bitBucketAccount, apiUrl);
    }

    public AutobumpUseCase getAutobumpUseCase() {
        return AutobumpUseCase.builder()
                .dependencyBumper(dependencyBumper)
                .dependencyResolver(dependencyResolver)
                .gitClient(gitClient)
                .gitProvider(gitProvider)
                .urlHelper(new BitBucketUrlHelper())
                .uri(url)
                .versionRepository(versionRepository)
                .build();
    }
}
