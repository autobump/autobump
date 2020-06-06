package com.github.autobump.springboot.services;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.internal.request.jwt.JwtBuilder;
import com.github.autobump.springboot.controllers.dtos.AccessTokenDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;

@Component
public class JwtFactory {

    @Value("${autobump.bitbucket.oAuthUrl}")
    private String oAuthUrl;
    @Value("$autobump.bitbucket.key")
    private String appKey;

    private final RestTemplateBuilder restTemplateBuilder;
    private final AtlassianHostRepository repository;

    @Autowired
    public JwtFactory(RestTemplateBuilder restTemplateBuilder, AtlassianHostRepository repository) {
        this.restTemplateBuilder = restTemplateBuilder;
        this.repository = repository;
    }

    public String getAccesToken() {
        var jwt = getJwt();
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "JWT " + jwt);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        var map = new LinkedMultiValueMap<String, String>();
        map.add("grant_type", "urn:bitbucket:oauth2:jwt");
        var entity = new HttpEntity<>(map, headers);
        return restTemplateBuilder.build().postForObject(oAuthUrl,
                entity,
                AccessTokenDto.class)
                .getToken();
    }

    public String getJwt() {
        AtlassianHost host = null;
        for (AtlassianHost atlassianHost : repository.findAll()) {
            host = atlassianHost;
        }

        if (host != null) {
            return new JwtBuilder()
                    .subject(host.getClientKey()) // = client key (retrieved on /install)
                    .issuer(appKey) // = app key
                    .signature(host.getSharedSecret()) // = shared secret (retrieved on /install)
                    .build();
        }
        return null;
    }
}
