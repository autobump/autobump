package com.github.autobump.springboot.repositories;

import com.github.autobump.core.model.domain.Repo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaRepoRepository extends JpaRepository<Repo, Long> {

    @Override
    List<Repo> findAll();

    Repo getByRepoId(String repoId);
}
