package com.github.autobump.cli.model;

import com.github.autobump.bitbucket.model.BitBucketAccount;
import com.github.autobump.bitbucket.model.BitBucketGitProvider;
import com.github.autobump.bitbucket.model.BitBucketGitProviderUrlHelper;
import com.github.autobump.core.model.AutobumpResult;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.GitProviderUrlHelper;
import com.github.autobump.core.model.IgnoreRepository;
import com.github.autobump.core.model.ReleaseNotesSource;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.core.model.usecases.AutobumpUseCase;
import com.github.autobump.core.model.usecases.BumpResolverUseCase;
import com.github.autobump.core.model.usecases.BumpUseCase;
import com.github.autobump.core.model.usecases.FetchVersionReleaseNotesUseCase;
import com.github.autobump.core.model.usecases.PostCommentOnPullRequestUseCase;
import com.github.autobump.core.model.usecases.PullRequestUseCase;
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
    @Mixin
    AutobumpPropertiesProvider properties = AutobumpPropertiesProvider.getInstance();

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new Autobump());
        cmd.execute(args);
        AutobumpResult result = cmd.getExecutionResult();
        spec.commandLine().getOut().println("amountBumped: " + result.getNumberOfBumps());
    }

    @Override
    public AutobumpResult call() {
        return getAutobumpUseCase().doAutoBump(properties.getUrl());
    }

    public AutobumpUseCase getAutobumpUseCase() {
        DependencyResolver dependencyResolver = new MavenDependencyResolver();
        DependencyBumper dependencyBumper = new MavenDependencyBumper();
        VersionRepository versionRepository = new MavenVersionRepository(properties.getRepositoryUrl());
        GitClient gitClient = new JGitGitClient(properties.getUsername(), properties.getPassword());
        BitBucketAccount bitBucketAccount = new BitBucketAccount(properties.getUsername(), properties.getPassword());
        GitProvider gitProvider = new BitBucketGitProvider(bitBucketAccount, properties.getBbApiUrl());
        ReleaseNotesSource releaseNotesSource = new GithubReleaseNotesSource(properties.getGhApiUrl());
        IgnoreRepository ignoreRepository = new MavenIgnoreRepository(properties.getIgnoreDependencies());
        GitProviderUrlHelper gitProviderUrlHelper = new BitBucketGitProviderUrlHelper();
        BumpResolverUseCase bumpResolverUseCase
                = BumpResolverUseCase.builder()
                .ignoreRepository(ignoreRepository)
                .versionRepository(versionRepository)
                .build();
        BumpUseCase bumpUseCase
                = BumpUseCase.builder()
                .dependencyBumper(dependencyBumper)
                .build();
        FetchVersionReleaseNotesUseCase fetchVersionReleaseNotesUseCase
                = FetchVersionReleaseNotesUseCase.builder()
                .releaseNotesSource(releaseNotesSource)
                .versionRepository(versionRepository)
                .build();
        PostCommentOnPullRequestUseCase postCommentOnPullRequestUseCase
                = PostCommentOnPullRequestUseCase.builder()
                .gitProvider(gitProvider)
                .urlHelper(gitProviderUrlHelper)
                .build();
        PullRequestUseCase pullRequestUseCase
                = PullRequestUseCase.builder()
                .gitClient(gitClient)
                .gitProvider(gitProvider)
                .gitProviderUrlHelper(gitProviderUrlHelper)
                .build();

        return AutobumpUseCase.builder()
                .bumpResolverUseCase(bumpResolverUseCase)
                .bumpUseCase(bumpUseCase)
                .fetchVersionReleaseNotesUseCase(fetchVersionReleaseNotesUseCase)
                .postCommentOnPullRequestUseCase(postCommentOnPullRequestUseCase)
                .pullRequestUseCase(pullRequestUseCase)
                .dependencyResolver(dependencyResolver)
                .gitClient(gitClient)
                .build();
    }
}
