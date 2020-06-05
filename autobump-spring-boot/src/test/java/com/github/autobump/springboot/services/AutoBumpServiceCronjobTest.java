package com.github.autobump.springboot.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@SpringBootTest
class AutoBumpServiceCronjobTest {

    @SpyBean
    private AutoBumpService autoBumpService;


    @Test
    void testCronjob() {
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> verify(autoBumpService, times(1)).autoBump());
    }

}
