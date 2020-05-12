package com.github.autobump.maven.model;

import com.github.autobump.core.model.Dependency;
import com.github.autobump.core.model.IgnoreRepository;
import com.github.autobump.core.model.Version;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MavenIgnoreRepositoryTest {
    private IgnoreRepository ignoreRepository;


    @BeforeEach
    void setUp() {
        Map<String, String> ignoreMap = new HashMap<>();
        ignoreMap.put("derby", "minor");
        ignoreMap.put("org.apache.derby", "major");
        ignoreMap.put("org.apache.derby:derby", "incremental");
        ignoreMap.put("org.springframework.boot:spring-boot-starter", "all");
        ignoreRepository = new MavenIgnoreRepository(ignoreMap);
    }

    @Test
    void isMajorIgnored() {
        Dependency dependency = Dependency.builder()
                .group("org.apache.derby")
                .name("test")
                .version(new MavenVersion("9.1.1.0"))
                .build();
        Version newVersion = new MavenVersion("10.15.2.0");
        assertTrue(ignoreRepository.isIgnored(dependency, newVersion));
    }

    @Test
    void isMinorIgnored() {
        Dependency dependency = Dependency.builder()
                .group("org.apache.nascar")
                .name("derby")
                .version(new MavenVersion("10.1.1.0"))
                .build();
        Version newVersion = new MavenVersion("10.15.2.0");
        assertTrue(ignoreRepository.isIgnored(dependency, newVersion));
    }

    @Test
    void isIncrermentalIgnored() {
        Dependency dependency = Dependency.builder()
                .group("org.apache.derby")
                .name("derby")
                .version(new MavenVersion("10.15.1.0"))
                .build();
        Version newVersion = new MavenVersion("10.15.1.1");
        Version newVersion1 = new MavenVersion("10.16.2.1");
        assertTrue(ignoreRepository.isIgnored(dependency, newVersion));
        assertFalse(ignoreRepository.isIgnored(dependency, newVersion1));
    }

    @Test
    void isAllIgnored() {
        Dependency dependency = Dependency.builder()
                .group("org.springframework.boot")
                .name("spring-boot-starter")
                .version(new MavenVersion("10.15.1.0"))
                .build();
        Version newVersion = new MavenVersion("10.15.2.0");
        Version newVersion1 = new MavenVersion("9.15.2.0");
        Version newVersion2 = new MavenVersion("10.14.2.0");
        assertTrue(ignoreRepository.isIgnored(dependency, newVersion));
        assertTrue(ignoreRepository.isIgnored(dependency, newVersion1));
        assertTrue(ignoreRepository.isIgnored(dependency, newVersion2));
    }

    @Test
    void testIsNotIgnored() {
        Dependency dependency = Dependency.builder()
                .group("org.springframework.boot")
                .name("spring-boot-starter-web")
                .version(new MavenVersion("10.15.1.0"))
                .build();
        Version newVersion = new MavenVersion("10.15.2.0");
        Version newVersion1 = new MavenVersion("9.15.2.0");
        Version newVersion2 = new MavenVersion("10.14.2.0");
        assertFalse(ignoreRepository.isIgnored(dependency, newVersion));
        assertFalse(ignoreRepository.isIgnored(dependency, newVersion1));
        assertFalse(ignoreRepository.isIgnored(dependency, newVersion2));
    }

}
