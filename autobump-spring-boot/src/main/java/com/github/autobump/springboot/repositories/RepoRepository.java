package com.github.autobump.springboot.repositories;

import com.github.autobump.core.model.Repo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepoRepository extends JpaRepository<Repo, Long> {

    @Override
    List<Repo> findAll();

    Repo getByRepoId(String repoId);
}
