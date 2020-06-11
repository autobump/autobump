package com.github.autobump.springboot.repositories;

import com.github.autobump.core.model.domain.Repo;
import com.github.autobump.core.repositories.RepoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class SpringRepoRepositoryTest {

    @Autowired
    private RepoRepository repoRepository;

    @Test
    void save() {
        Repo repo = getRepo1();
        assertThat(repoRepository.save(repo)).isEqualToComparingFieldByField(getRepo1());
    }

    @Test
    void saveAllRepos() {
        List<Repo> repos = getDummyRepoList();
        assertThat(repoRepository.saveAllRepos(repos).size()).isEqualTo(2);
    }

    @Test
    void getByRepoId() {
        assertThat(repoRepository.getByRepoId("cjhcvkjbub")).isEqualToComparingFieldByField(getRepo1());
    }

    @Test
    void findAll() {
        repoRepository.saveAllRepos(getDummyRepoList());
        assertThat(repoRepository.findAll().size()).isEqualTo(2);
    }

    @Test
    void delete() {
        repoRepository.save(getRepo1());
        repoRepository.delete(getRepo1());
        assertThat(repoRepository.getByRepoId(getRepo1().getRepoId())).isNull();
    }

    private List<Repo> getDummyRepoList() {
        List<Repo> repos = new ArrayList<>();
        Repo repo = getRepo1();
        repos.add(repo);
        Repo repo2 = getRepo2();
        repos.add(repo2);
        return repos;
    }

    private Repo getRepo1() {
        Repo repo = new Repo();
        repo.setName("MultiModuleMavenProject");
        repo.setSelected(true);
        repo.setLink("a_link");
        repo.setRepoId("cjhcvkjbub");
        return repo;
    }

    private Repo getRepo2() {
        Repo repo2 = new Repo();
        repo2.setName("TestMavenProject");
        repo2.setSelected(false);
        repo2.setLink("another_link");
        repo2.setRepoId("emofbbSbgB");
        return repo2;
    }



}
