package com.github.autobump.springboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.github.autobump")
@EntityScan("com.github.autobump")
public class AutobumpSpringBoot {
    public static void main(String[] args) {
        SpringApplication.run(AutobumpSpringBoot.class, args);
    }
}
