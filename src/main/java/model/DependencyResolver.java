package model;

import java.util.Set;

public interface DependencyResolver {
    Set<Dependency> resolve(Workspace workspace);
}
