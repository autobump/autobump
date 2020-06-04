package com.github.autobump.springboot.repositories;

import com.github.autobump.core.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface JpaSettingsRepository extends JpaRepository<Setting, String> {

    List<Setting> findAllByRepositoryName(String repositoryName);

    void deleteByTypeAndRepositoryName(Setting.SettingsType type, String repoName);
}
