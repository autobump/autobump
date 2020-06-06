package com.github.autobump.springboot.interceptors;

import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class JwtInterceptorTest {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @BeforeEach
    void setUp() {
//        interceptor = new JwtInterceptor(jwtFactory);
    }

    @Test
    void apply() {
        RequestTemplate requestTemplate = new RequestTemplate();
        jwtInterceptor.apply(requestTemplate);
        assertThat(requestTemplate.headers().keySet()).contains("Authorization");
    }
}
