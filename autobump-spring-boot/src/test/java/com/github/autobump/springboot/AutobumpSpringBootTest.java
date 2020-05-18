package com.github.autobump.springboot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThatCode;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class AutobumpSpringBootTest {
    @Test
    public void main_applicationStarts() {
        String[] args = {};
        assertThatCode(() -> AutobumpSpringBoot.main(args)).doesNotThrowAnyException();
    }
}
