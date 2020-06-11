package com.github.autobump.core.repositories;

import com.github.autobump.core.model.domain.Repo;

import java.util.List;

public interface RepoRepository {

    Repo save(Repo repo);

    List<Repo> saveAllRepos(List<Repo> repos);

    Repo getByRepoId(String repoId);

    List<Repo> findAll();

    void delete(Repo repo);

    void deleteAll();
}
