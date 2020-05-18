package com.github.autobump.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan("com.github.autobump")
@EntityScan("com.github.autobump")
@EnableJpaRepositories
public class AutobumpSpringBoot {
    public static void main(String[] args) {
        SpringApplication.run(AutobumpSpringBoot.class, args);
    }
}
