package com.github.autobump.springboot.repositories;

import com.github.autobump.core.model.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface JpaSettingsRepository extends JpaRepository<Setting, String> {
}
