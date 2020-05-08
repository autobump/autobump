package com.github.autobump.core.model.usecases;

import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.Workspace;
import lombok.Builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Builder
public class ResolveDependenciesUseCase {
    private final DependencyResolver dependencyResolver;
    private final Workspace workspace;

    public ResolveDependenciesUseCase(DependencyResolver dependencyResolver, Workspace workspace) {
        this.dependencyResolver = dependencyResolver;
        this.workspace = workspace;
    }

    public Map<String, Set<Dependency>> deResolve(){
        Map<String, Set<Dependency>> dependencyMap = new HashMap<>();
        for (Dependency dependency : dependencyResolver.resolve(workspace)) {
            String key = String.format("%s %s", dependency.getGroup(), dependency.getVersion().getVersionNumber());
            if (dependencyMap.containsKey(dependency.getGroup() + dependency.getVersion().getVersionNumber())){
                dependencyMap.get(key).add(dependency);
            }else {
                dependencyMap.put(key, new HashSet<>());
                dependencyMap.get(key).add(dependency);
            }
        }
        return dependencyMap;
    }
}
