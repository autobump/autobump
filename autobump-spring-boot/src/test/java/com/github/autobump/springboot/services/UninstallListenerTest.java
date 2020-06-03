package com.github.autobump.springboot.services;

import com.atlassian.connect.spring.AddonUninstalledEvent;
import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class UninstallListenerTest {

    @Autowired
    private AtlassianHostRepository repository;

    @Autowired
    private UninstallListener listener;


    @BeforeEach
    void setUp() {

    }

    @Test
    void onApplicationEvent() {
        var host = new AtlassianHost();
        host.setClientKey("test");
        host.setSharedSecret("test");
        repository.save(host);
        assertThat(repository.findAll()).isNotEmpty();
        listener.onApplicationEvent(new AddonUninstalledEvent("testSource", host));
        assertThat(repository.findAll()).isEmpty();
    }
}
