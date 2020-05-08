package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.AutobumpResult;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.UrlHelper;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.core.model.Workspace;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.net.URI;
import java.util.Map;
import java.util.Set;

@Data
public class AutobumpUseCase {
    @NonNull
    private final GitProvider gitProvider;
    @NonNull
    private final GitClient gitClient;
    @NonNull
    private final DependencyResolver dependencyResolver;
    @NonNull
    private final VersionRepository versionRepository;
    @NonNull
    private final DependencyBumper dependencyBumper;
    @NonNull
    private final UrlHelper urlHelper;
    @NonNull
    private final URI uri;
    private int amountofbumps;

    @Builder
    public AutobumpUseCase(GitProvider gitProvider,
                           GitClient gitClient,
                           DependencyResolver dependencyResolver,
                           VersionRepository versionRepository,
                           DependencyBumper dependencyBumper,
                           UrlHelper urlHelper,
                           URI uri) {
        this.gitProvider = gitProvider;
        this.gitClient = gitClient;
        this.dependencyResolver = dependencyResolver;
        this.versionRepository = versionRepository;
        this.dependencyBumper = dependencyBumper;
        this.urlHelper = urlHelper;
        this.uri = uri;
    }

    public AutobumpResult doAutoBump() {
        Workspace workspace = gitClient.clone(getUri());
        var dependencymap = ResolveDependenciesUseCase.builder()
                .dependencyResolver(dependencyResolver)
                .workspace(workspace)
                .build()
                .deResolve();
        makeBumpsAndPullRequests(workspace, dependencymap);
        return new AutobumpResult(amountofbumps);
    }

    private void makeBumpsAndPullRequests(Workspace workspace, Map<String,
            Set<Dependency>> dependencymap) {
        for (var entry : dependencymap.entrySet()) {
            boolean bumped = false;
            for (Dependency dependency : dependencymap.get(entry.getKey())) {
                Version latestVersion = getLatestVersion(dependency);
                if (latestVersion != null &&
                        dependency.getVersion().compareTo(latestVersion) > 0) {
                    BumpUseCase.builder()
                            .dependency(dependency)
                            .dependencyBumper(dependencyBumper)
                            .latestVersion(latestVersion)
                            .workspace(workspace)
                            .build().doBump();
                    bumped = isBumped();
                }
            }
            if (bumped) {
                PullRequestUseCase.builder()
                        .gitProvider(gitProvider).gitClient(gitClient)
                        .urlHelper(urlHelper).workspace(workspace)
                        .groupId(entry.getKey().split(" ")[0])
                        .version(entry.getKey().split(" ")[1])
                        .uri(uri).build()
                        .doPullRequest();
            }
        }
    }

    private boolean isBumped() {
        amountofbumps++;
        return true;
    }

    private Version getLatestVersion(Dependency dependency) {
        return versionRepository.getAllAvailableVersions(dependency).stream()
                .sorted().findFirst().orElse(null);
    }
}
