package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.AutobumpResult;
import com.github.autobump.core.model.Bump;
import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.IgnoreRepository;
import com.github.autobump.core.model.UrlHelper;
import com.github.autobump.core.model.Version;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.core.model.Workspace;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
    @NonNull
    private final IgnoreRepository ignoreRepository;

    @Builder
    public AutobumpUseCase(GitProvider gitProvider,
                           GitClient gitClient,
                           DependencyResolver dependencyResolver,
                           VersionRepository versionRepository,
                           DependencyBumper dependencyBumper,
                           UrlHelper urlHelper,
                           IgnoreRepository ignoreRepository,
                           URI uri) {
        this.gitProvider = gitProvider;
        this.gitClient = gitClient;
        this.dependencyResolver = dependencyResolver;
        this.versionRepository = versionRepository;
        this.dependencyBumper = dependencyBumper;
        this.urlHelper = urlHelper;
        this.ignoreRepository = ignoreRepository;
        this.uri = uri;
    }

    public AutobumpResult doAutoBump() {
        Workspace workspace = gitClient.clone(getUri());
        Set<Dependency> dependencies = dependencyResolver.resolve(workspace);
        var bumps = makeBumpSet(dependencies);
        var combinedbumps = groupBumps(bumps);
        makeBumpsAndPullRequests(workspace, combinedbumps);
        return new AutobumpResult(bumps.size());
    }

    private Set<Bump> groupBumps(Set<Bump> bumps) {
        Set<Bump> groupedBumps = bumps.stream().collect(Collectors.collectingAndThen(
                Collectors.toUnmodifiableMap(b -> b.getGroup() + ":" + b.getUpdatedVersion(), b -> b, Bump::combine),
                map -> Set.copyOf(map.values())));
        return groupedBumps;
    }

    private Set<Bump> makeBumpSet(Set<Dependency> dependencies) {
        Set<Bump> bumps = new HashSet<>();
        for (Dependency dependency : dependencies) {
            Version latestVersion = getUpdateVersion(dependency);
            if (latestVersion != null && dependency.getVersion().compareTo(latestVersion) > 0) {
                //newer version is found => make bump
                bumps.add(new Bump(Set.of(dependency), latestVersion));
            }
        }
        return bumps;
    }

    private void makeBumpsAndPullRequests(Workspace workspace, Set<Bump> bumps) {
        //bumpUsecase
        //prUsecase
        for (Bump bump : bumps) {
            BumpUseCase.builder()
                    .dependencyBumper(dependencyBumper)
                    .workspace(workspace)
                    .bump(bump)
                    .build()
                    .doBump();
            PullRequestUseCase.builder()
                    .uri(uri)
                    .gitProvider(gitProvider)
                    .gitClient(gitClient)
                    .urlHelper(urlHelper)
                    .workspace(workspace)
                    .bump(bump)
                    .build()
                    .doPullRequest();
        }

    }


    private Version getUpdateVersion(Dependency dependency) {
        Version latestVersion = getLatestVersion(dependency);
        if (latestVersion != null && ignoreRepository.isIgnored(dependency, latestVersion)) {
            latestVersion = null;
        }
        return latestVersion;
    }

    private Version getLatestVersion(Dependency dependency) {
        return versionRepository.getAllAvailableVersions(dependency).stream()
                .sorted().findFirst().orElse(null);
    }
}
