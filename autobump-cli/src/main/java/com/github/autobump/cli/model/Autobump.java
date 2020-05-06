package com.github.autobump.cli.model;

import com.github.autobump.bitbucket.model.BitBuckeUrltHelper;
import com.github.autobump.bitbucket.model.BitBucketAccount;
import com.github.autobump.bitbucket.model.BitBucketGitProvider;
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

import java.net.URI;
import java.util.concurrent.Callable;

@Command(
        name = "autobump",
        description = "automatically creates a pull-request for every outdated dependency in a project"
)
public class Autobump implements Callable<Integer> {
    private final String apiUrl;
    private final DependencyResolver dependencyResolver = new MavenDependencyResolver();
    private final VersionRepository versionRepository;
    private final DependencyBumper dependencyBumper = new MavenDependencyBumper();
    private GitClient gitClient;
    private GitProvider gitProvider;
    @Option(names = {"-u", "--user"}, description = "User name", required = true)
    private String username;
    @Option(names = {"-l", "--url"}, paramLabel = "REPOURL", description = "project repository url", required = true)
    private URI url;
    @Option(names = {"-p", "--password"}, description = "Passphrase", required = true)
    private String password;

    public Autobump(String repositoryUrl, String apiUrl) {
        this.apiUrl = apiUrl;
        versionRepository = new MavenVersionRepository(repositoryUrl);
    }

    public static void main(String[] args) {
        new CommandLine(
                new Autobump(
                        "https://repo1.maven.org/maven2",
                        "https://api.bitbucket.org/2.0"))
                .execute(args);
    }

    @Override
    public Integer call() throws Exception {

        initialize();
        AutobumpUseCase.builder()
                .dependencyBumper(dependencyBumper)
                .dependencyResolver(dependencyResolver)
                .gitClient(gitClient)
                .gitProvider(gitProvider)
                .urlHelper(new BitBuckeUrltHelper())
                .uri(url)
                .versionRepository(versionRepository)
                .build()
                .execute();
        return 0;
    }


    private void initialize() {
        gitClient = new JGitGitClient(username, password);
        BitBucketAccount bitBucketAccount = new BitBucketAccount(username, password);
        gitProvider = new BitBucketGitProvider(bitBucketAccount, apiUrl);
    }


}
