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
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.core.model.Workspace;
import com.github.autobump.jgit.model.JGitGitClient;
import com.github.autobump.maven.model.MavenDependencyBumper;
import com.github.autobump.maven.model.MavenDependencyResolver;
import com.github.autobump.maven.model.MavenVersion;
import com.github.autobump.maven.model.MavenVersionRepository;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.Callable;

@Command(
        name = "autobump",
        description = "automatically creates a pull-request for every outdated dependency in a project"
)
public class Autobump implements Callable<Integer> {
    private static final String MAVENREPOSITORY_URL = "https://repo1.maven.org/maven2";
    private static final String BITBUCKET_API_URL = "https://api.bitbucket.org/2.0";
    private final DependencyResolver dependencyResolver = new MavenDependencyResolver();
    private final VersionRepository versionRepository = new MavenVersionRepository(MAVENREPOSITORY_URL);
    private final DependencyBumper dependencyBumper = new MavenDependencyBumper();
    private GitClient gitClient;
    private GitProvider gitProvider;
    @Option(names = {"-u", "--user"}, description = "User name", required = true)
    private String username;
    @Option(names = {"-l", "--url"}, paramLabel = "REPOURL", description = "project repository url", required = true)
    private String url;
    @Option(names = {"-p", "--password"}, description = "Passphrase", required = true)
    private String password;

    public static void main(String[] args) {
        int exitcode = new CommandLine(new Autobump()).execute(args);
        if (exitcode != 0) {
            throw new CommandLineException(Integer.toString(exitcode));
        }
    }

    @Override
    public Integer call() throws Exception {
        initialize();
        Workspace workspace = gitClient.clone(new URI(url));
        Set<Dependency> dependencySet = dependencyResolver.resolve(workspace);

        for (Dependency dependency : dependencySet) {
            Version latestVersion = versionRepository.getAllAvailableVersions(dependency).stream()
                    .sorted().findFirst().orElse(null);
            if (latestVersion != null && new MavenVersion(dependency.getVersion()).compareTo(latestVersion) > 0) {
                Bump bump = doBump(workspace, dependency, latestVersion);
                makePullRequest(workspace, bump);
            }
        }
        return 0;
    }

    private void initialize() {
        gitClient = new JGitGitClient(username, password);
        BitBucketAccount bitBucketAccount = new BitBucketAccount(username, password);
        gitProvider = new BitBucketGitProvider(bitBucketAccount, BITBUCKET_API_URL);
    }

    private void makePullRequest(Workspace workspace, Bump bump) {
        var commitResult = gitClient.commitToNewBranch(workspace, bump);
        PullRequest pullRequest = PullRequest.builder()
                .branchName(commitResult.getBranchName())
                .title(commitResult.getCommitMessage())
                .repoName(BitBucketHelper.getRepoName(url))
                .repoOwner(BitBucketHelper.getOwnerName(url))
                .build();
        gitProvider.makePullRequest(pullRequest);
    }

    private Bump doBump(Workspace workspace, Dependency dependency, Version latestVersion) {
        Bump bump = new Bump(dependency, latestVersion);
        dependencyBumper.bump(workspace, bump);
        return bump;
    }
}
