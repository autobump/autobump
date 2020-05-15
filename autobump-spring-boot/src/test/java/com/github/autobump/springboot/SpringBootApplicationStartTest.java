package com.github.autobump.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class SpringBootApplicationStartTest {
    @Test
    public void applicationStarts() {
        AutobumpSpringBoot.main(new String[] {});
    }
}
