package com.github.autobump.springboot.interceptors;

import com.github.autobump.springboot.services.JwtFactory;
import feign.RequestInterceptor;
import feign.RequestTemplate;

public class JwtInterceptor implements RequestInterceptor {

    private final JwtFactory jwtFactory;

    public JwtInterceptor(JwtFactory jwtFactory) {
        this.jwtFactory = jwtFactory;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", "JWT " + jwtFactory.getJwt());
    }
}

//
//public class JwtInterceptor implements RequestInterceptor {
//
//    private final String jwt;
//
//    public JwtInterceptor(String jwt) {
//        this.jwt = jwt;
//    }
//
//    @Override
//    public void apply(RequestTemplate template) {
//        template.header("Authorization", "JWT " + jwt);
//    }
//}
