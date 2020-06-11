package com.github.autobump.cli.model;

import com.github.autobump.bitbucket.model.BitBucketAccount;
import com.github.autobump.bitbucket.model.BitBucketGitProvider;
import com.github.autobump.bitbucket.model.BitBucketGitProviderUrlHelper;
import com.github.autobump.cli.repositories.CliSettingsRepository;
import com.github.autobump.core.model.dependencymanagement.DependencyBumper;
import com.github.autobump.core.model.dependencymanagement.DependencyResolver;
import com.github.autobump.core.model.gitclients.GitClient;
import com.github.autobump.core.model.gitproviders.GitProvider;
import com.github.autobump.core.model.releasenotes.ReleaseNotesSource;
import com.github.autobump.core.model.results.AutobumpResult;
import com.github.autobump.core.repositories.IgnoreRepository;
import com.github.autobump.core.repositories.VersionRepository;
import com.github.autobump.core.usecases.AutobumpUseCase;
import com.github.autobump.core.usecases.UseCaseConfiguration;
import com.github.autobump.github.model.GithubReleaseNotesSource;
import com.github.autobump.jgit.model.JGitGitClient;
import com.github.autobump.maven.model.MavenDependencyBumper;
import com.github.autobump.maven.model.MavenDependencyResolver;
import com.github.autobump.maven.model.MavenIgnoreRepository;
import com.github.autobump.maven.model.MavenVersionRepository;
import picocli.CommandLine;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.util.concurrent.Callable;

public class Autobump implements Callable<AutobumpResult> {
    @Spec
    private static CommandSpec spec;
    private final DependencyResolver dependencyResolver = new MavenDependencyResolver();
    private final DependencyBumper dependencyBumper = new MavenDependencyBumper();
    @Mixin
    private final AutobumpPropertiesProvider properties = AutobumpPropertiesProvider.getInstance();
    private ReleaseNotesSource releaseNotesSource;
    private VersionRepository versionRepository;
    private GitClient gitClient;
    private GitProvider gitProvider;
    private IgnoreRepository ignoreRepository;

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Autobump());
        cmd.execute(args);
        AutobumpResult result = cmd.getExecutionResult();
        spec.commandLine().getOut().println(result == null ? "" : "amountBumped: " + result.getNumberOfBumps());
    }

    @Override
    public AutobumpResult call() {
        initialize();
        return getAutobumpUseCase().doAutoBump();
    }

    private void initialize() {
        versionRepository = new MavenVersionRepository(properties.getRepositoryUrl());
        gitClient = new JGitGitClient(properties.getUsername(), properties.getPassword());
        BitBucketAccount bitBucketAccount = new BitBucketAccount(properties.getUsername(), properties.getPassword());
        gitProvider = new BitBucketGitProvider(bitBucketAccount, properties.getBbApiUrl());
        releaseNotesSource = new GithubReleaseNotesSource(properties.getGhApiUrl());
        ignoreRepository = new MavenIgnoreRepository(properties.getIgnoreDependencies());
    }

    public AutobumpUseCase getAutobumpUseCase() {
        UseCaseConfiguration config = UseCaseConfiguration.builder()
                .gitClient(gitClient)
                .dependencyBumper(dependencyBumper)
                .dependencyResolver(dependencyResolver)
                .gitProvider(gitProvider)
                .versionRepository(versionRepository)
                .gitProviderUrlHelper(new BitBucketGitProviderUrlHelper())
                .ignoreRepository(ignoreRepository)
                .build();
        return AutobumpUseCase.builder()
                .config(config)
                .settingsRepository(new CliSettingsRepository())
                .uri(properties.getUrl())
                .releaseNotesSource(releaseNotesSource)
                .build();
    }
}
