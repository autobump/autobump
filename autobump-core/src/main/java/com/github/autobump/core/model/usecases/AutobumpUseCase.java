package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.AutobumpResult;
import com.github.autobump.core.model.Bump;
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

@Data
@Builder
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



    public AutobumpResult doAutoBump() {
        int amountOfBumps = 0;
        Workspace workspace = gitClient.clone(getUri());
        for (Dependency dependency : dependencyResolver.resolve(workspace)) {
            Version latestVersion = getLatestVersion(dependency);
            if (latestVersion != null &&
                    dependency.getVersion().compareTo(latestVersion) > 0) {
                Bump bump = BumpUseCase.builder()
                        .dependency(dependency)
                        .dependencyBumper(dependencyBumper)
                        .latestVersion(latestVersion)
                        .workspace(workspace)
                        .build()
                        .doBump();
                PullRequestUseCase.builder()
                        .gitProvider(gitProvider)
                        .gitClient(gitClient)
                        .urlHelper(urlHelper)
                        .workspace(workspace)
                        .bump(bump)
                        .uri(uri)
                        .build()
                        .doPullRequest();
                amountOfBumps++;
            }
        }
        return new AutobumpResult(amountOfBumps);
    }

    private Version getLatestVersion(Dependency dependency) {
        return versionRepository.getAllAvailableVersions(dependency).stream()
                .sorted().findFirst().orElse(null);
    }


}
