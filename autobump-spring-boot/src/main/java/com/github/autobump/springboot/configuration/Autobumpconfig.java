package com.github.autobump.springboot.configuration;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.internal.request.jwt.JwtBuilder;
import com.github.autobump.bitbucket.model.BitBucketGitProvider;
import com.github.autobump.bitbucket.model.BitBucketUrlHelper;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.IgnoreRepository;
import com.github.autobump.core.model.UrlHelper;
import com.github.autobump.core.model.UseCaseConfiguration;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.jgit.model.JGitGitClient;
import com.github.autobump.maven.model.MavenDependencyBumper;
import com.github.autobump.maven.model.MavenDependencyResolver;
import com.github.autobump.maven.model.MavenIgnoreRepository;
import com.github.autobump.maven.model.MavenVersionRepository;
import com.github.autobump.springboot.controllers.dtos.AccessTokenDto;
import com.github.autobump.springboot.interceptors.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;

@Configuration
public class Autobumpconfig {

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;
    @Autowired
    private AtlassianHostRepository repository;


    public UseCaseConfiguration setupConfig() {
        return UseCaseConfiguration.builder()
                .dependencyBumper(getMavenDependencyBumper())
                .dependencyResolver(getDependencyResolver())
                .gitClient(getGitClient())
                .gitProvider(getGitProvider())
                .ignoreRepository(getIgnoreRepo())
                .urlHelper(getUrlHelper())
                .versionRepository(getVersionRepository())
                .build();
    }

    private DependencyBumper getMavenDependencyBumper() {
        return new MavenDependencyBumper();
    }

    private DependencyResolver getDependencyResolver() {
        return new MavenDependencyResolver();
    }

    private GitClient getGitClient() {
        return new JGitGitClient("x-token-auth", getAccesToken());
    }

    private GitProvider getGitProvider() {
        return new BitBucketGitProvider(new JwtInterceptor(getJwt()));
    }

    private IgnoreRepository getIgnoreRepo() {
        return new MavenIgnoreRepository(null);
    }

    private UrlHelper getUrlHelper() {
        return new BitBucketUrlHelper();
    }

    private VersionRepository getVersionRepository() {
        return new MavenVersionRepository();
    }

    private String getAccesToken() {
        var jwt = getJwt();
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "JWT " + jwt);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        var map = new LinkedMultiValueMap<String, String>();
        map.add("grant_type", "urn:bitbucket:oauth2:jwt");
        var entity = new HttpEntity<>(map, headers);
        return restTemplateBuilder.build().postForObject("https://bitbucket.org/site/oauth2/access_token", entity, AccessTokenDto.class).getToken();
    }

    private String getJwt(){
        AtlassianHost host = null;
        for (AtlassianHost atlassianHost : repository.findAll()) {
            host = atlassianHost;
            break;
        }

        return new JwtBuilder()
                .subject(host.getClientKey()) // = client key (retrieved on /install)
                .issuer("autobump.kdg.xplore.dev02") // = app key
                .signature(host.getSharedSecret()) // = shared secret (retrieved on /install)
                .build();
    }
}
