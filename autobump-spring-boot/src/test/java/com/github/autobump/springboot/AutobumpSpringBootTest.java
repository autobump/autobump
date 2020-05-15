package com.github.autobump.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatCode;

@ActiveProfiles("test")
public class AutobumpSpringBootTest {
    @Test
    public void main_applicationStarts() {
        String[] args = {};
        assertThatCode(() -> AutobumpSpringBoot.main(args)).doesNotThrowAnyException();
    }
}
