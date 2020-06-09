package com.github.autobump.core.model;

import java.util.List;

public interface RepoRepository {

    Repo save(Repo repo);

    List<Repo> saveAllRepos(List<Repo> repos);

    Repo getByRepoId(String repoId);

    List<Repo> findAll();

    void delete(Repo repo);

    void deleteAll();
}
