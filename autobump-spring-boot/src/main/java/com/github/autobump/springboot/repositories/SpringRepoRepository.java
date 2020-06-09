package com.github.autobump.springboot.repositories;

import com.github.autobump.core.model.Repo;
import com.github.autobump.core.model.RepoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SpringRepoRepository implements RepoRepository {
    private final JpaRepoRepository jpaRepoRepository;

    public SpringRepoRepository(JpaRepoRepository jpaRepoRepository) {
        this.jpaRepoRepository = jpaRepoRepository;
    }

    @Override
    public Repo save(Repo repo) {
        return jpaRepoRepository.save(repo);
    }

    @Override
    public List<Repo> saveAllRepos(List<Repo> repos) {
        return jpaRepoRepository.saveAll(repos);
    }

    @Override
    public Repo getByRepoId(String repoId) {
        return jpaRepoRepository.getByRepoId(repoId);
    }

    @Override
    public List<Repo> findAll() {
        return jpaRepoRepository.findAll();
    }

    @Override
    public void delete(Repo repo) {
        jpaRepoRepository.delete(repo);
    }

    @Override
    public void deleteAll() {
        jpaRepoRepository.deleteAll();
    }
}
