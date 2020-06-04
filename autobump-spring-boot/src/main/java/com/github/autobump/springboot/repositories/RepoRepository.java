package com.github.autobump.springboot.repositories;

import com.github.autobump.springboot.domain.Repo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepoRepository extends JpaRepository<Repo, Long> {
    List<Repo> findAll();
    Repo getByRepoId(int repoId);
}
