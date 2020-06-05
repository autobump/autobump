package com.github.autobump.springboot.configuration;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.internal.request.jwt.JwtBuilder;
import com.github.autobump.bitbucket.model.BitBucketGitProvider;
import com.github.autobump.bitbucket.model.BitBucketGitProviderUrlHelper;
import com.github.autobump.core.model.DependencyBumper;
import com.github.autobump.core.model.DependencyResolver;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.GitProviderUrlHelper;
import com.github.autobump.core.model.IgnoreRepository;
import com.github.autobump.core.model.UseCaseConfiguration;
import com.github.autobump.core.model.VersionRepository;
import com.github.autobump.jgit.model.JGitGitClient;
import com.github.autobump.maven.model.MavenDependencyBumper;
import com.github.autobump.maven.model.MavenDependencyResolver;
import com.github.autobump.maven.model.MavenIgnoreRepository;
import com.github.autobump.maven.model.MavenVersionRepository;
import com.github.autobump.springboot.controllers.dtos.AccessTokenDto;
import com.github.autobump.springboot.interceptors.JwtInterceptor;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Configuration
public class Autobumpconfig {

    private final RestTemplate restTemplateBuilder;
    private final AtlassianHostRepository repository;
    @Value("${autobump.bitbucket.oAuthUrl}")
    private String oAuthUrl;

    public Autobumpconfig(RestTemplateBuilder restTemplateBuilder, AtlassianHostRepository repository) {
        this.restTemplateBuilder = restTemplateBuilder
                .requestFactory(() -> {
                    var factory = new OkHttp3ClientHttpRequestFactory(
                            new OkHttpClient().newBuilder().connectionPool(
                                    new ConnectionPool(1, 5L, TimeUnit.MINUTES))
                                    .build());

                    factory.setConnectTimeout(2_000);
                    factory.setReadTimeout(5_000);
                    factory.setWriteTimeout(5_000);

                    return factory;
                })
                .build();
        this.repository = repository;
    }


    public UseCaseConfiguration setupConfig() {
        return UseCaseConfiguration.builder()
                .dependencyBumper(getMavenDependencyBumper())
                .dependencyResolver(getDependencyResolver())
                .gitClient(getGitClient())
                .gitProvider(getGitProvider())
                .ignoreRepository(getIgnoreRepo())
                .gitProviderUrlHelper(getUrlHelper())
                .versionRepository(getVersionRepository())
                .build();
    }

    public DependencyBumper getMavenDependencyBumper() {
        return new MavenDependencyBumper();
    }

    public DependencyResolver getDependencyResolver() {
        return new MavenDependencyResolver();
    }

    public GitClient getGitClient() {
        return new JGitGitClient("x-token-auth", getAccesToken());
    }

    public GitProvider getGitProvider() {
        return new BitBucketGitProvider(new JwtInterceptor(getJwt()));
    }

    public IgnoreRepository getIgnoreRepo() {
        return new MavenIgnoreRepository(null);
    }

    public GitProviderUrlHelper getUrlHelper() {
        return new BitBucketGitProviderUrlHelper();
    }

    public VersionRepository getVersionRepository() {
        return new MavenVersionRepository();
    }

    public String getAccesToken() {
        var jwt = getJwt();
        var headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "JWT " + jwt);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        var map = new LinkedMultiValueMap<String, String>();
        map.add("grant_type", "urn:bitbucket:oauth2:jwt");
        var entity = new HttpEntity<>(map, headers);
        return restTemplateBuilder.postForObject(oAuthUrl,
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
                    .issuer("autobump.kdg.xplore.dev02") // = app key
                    .signature(host.getSharedSecret()) // = shared secret (retrieved on /install)
                    .build();
        }
        return null;
    }
}
