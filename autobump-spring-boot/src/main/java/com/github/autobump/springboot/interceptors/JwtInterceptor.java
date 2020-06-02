package com.github.autobump.springboot.interceptors;

import feign.RequestInterceptor;
import feign.RequestTemplate;


public class JwtInterceptor implements RequestInterceptor {

    private final String jwt;

    public JwtInterceptor(String jwt) {
        this.jwt = jwt;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", "JWT " + jwt);
    }
}
