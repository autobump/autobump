package com.github.autobump.cli.model;

import com.github.autobump.bitbucket.model.BitBucketAccount;
import com.github.autobump.bitbucket.model.BitBucketGitProvider;
import com.github.autobump.bitbucket.model.BitBucketHelper;
import com.github.autobump.cli.exceptions.CommandLineException;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.PullRequestResponse;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.jgit.model.JGitGitClient;
import com.github.autobump.maven.model.MavenDependencyBumper;
import com.github.autobump.maven.model.MavenDependencyResolver;
import com.github.autobump.maven.model.MavenVersion;
import com.github.autobump.maven.model.MavenVersionRepository;
import lombok.extern.java.Log;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

@Command(
        name = "autobump",
        description = "automatically creates a pull-request for every outdated dependency in a project"
)
public class Autobump implements Callable<Integer> {
    private static final String MAVENREPOSITORY_URL = "https://repo1.maven.org/maven2";
    private static final String BITBUCKET_API_URL = "api.bitbucket.org/2.0";
    @CommandLine.Option(names = {"-u", "--user"}, description = "User name", required = true)
    String username;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Passphrase", required = true)
    String password;
    @CommandLine.Option(
            names = {
                    "-l", "--url"
            },
            paramLabel = "REPOURL",
            description = "project repository url",
            required = true
    )
    private String url;

    public static void main(String[] args) {
        int exitcode = new CommandLine(new Autobump()).execute(args);
        if (exitcode != 0) {
            throw new CommandLineException(Integer.toString(exitcode));
        }
    }

    @Override
    public Integer call() throws Exception {
        Logger.getAnonymousLogger().info(String.format("username: %s\npassword: %s\nurl: %s", username, password, url));

        GitClient gitClient = new JGitGitClient();
        Workspace workspace = gitClient.clone(new URI(url));
        DependencyResolver dependencyResolver = new MavenDependencyResolver();
        Set<Dependency> dependencySet = dependencyResolver.resolve(workspace);
        VersionRepository versionRepository = new MavenVersionRepository(MAVENREPOSITORY_URL);
        DependencyBumper dependencyBumper = new MavenDependencyBumper();
        BitBucketAccount bitBucketAccount = new BitBucketAccount(username, password);
        GitProvider gitProvider = new BitBucketGitProvider(bitBucketAccount, BITBUCKET_API_URL);
        for (Dependency dependency : dependencySet) {
            Version latestVersion = versionRepository.getAllAvailableVersions(dependency).stream()
                    .sorted().findFirst().orElse(null);
            for (Version allAvailableVersion : versionRepository.getAllAvailableVersions(dependency)) {
                Logger.getAnonymousLogger().info("- " + allAvailableVersion.getVersionNumber());
            }
            Logger.getAnonymousLogger().info(dependency.getName() + ": " + dependency.getVersion());
            if (latestVersion != null && new MavenVersion(dependency.getVersion()).compareTo(latestVersion) < 0) {
                Bump bump = new Bump(dependency, latestVersion);
                dependencyBumper.bump(workspace, bump);
                String branchName = gitClient.commitToNewBranch(workspace, bump);
                PullRequest pullRequest = PullRequest.builder()
                        .branchName(branchName)
                        .title("Autobump Test")
                        .repoName(BitBucketHelper.getRepoName(url))
                        .repoOwner(BitBucketHelper.getOwnerName(url))
                        .build();
                PullRequestResponse pullRequestResponse = gitProvider.makePullRequest(pullRequest);
                Logger.getAnonymousLogger().info(pullRequestResponse.toString());
            }
        }
        return 0;
    }
}
