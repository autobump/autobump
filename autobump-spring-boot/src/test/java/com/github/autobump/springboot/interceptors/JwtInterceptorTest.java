package com.github.autobump.springboot.interceptors;

import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtInterceptorTest {

    private JwtInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new JwtInterceptor("testJwt");
    }

    @Test
    void apply() {
        RequestTemplate requestTemplate = new RequestTemplate();
        interceptor.apply(requestTemplate);
        assertThat(requestTemplate.headers().keySet()).contains("Authorization");
    }
}
