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
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

@Configuration
public class Autobumpconfig implements WebMvcConfigurer {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;
    @Autowired
    private AtlassianHostRepository repository;
    @Value("${autobump.bitbucket.oAuthUrl}")
    private String oAuthUrl;


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

    @Bean
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
        return restTemplateBuilder.build().postForObject(oAuthUrl,
                entity,
                AccessTokenDto.class)
                .getToken();
    }

    public String getJwt(){
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

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        return templateResolver;
    }

    /*
     * STEP 2 - Create SpringTemplateEngine
     * */
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        return templateEngine;
    }

    /*
     * STEP 3 - Register ThymeleafViewResolver
     * */
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        registry.viewResolver(resolver);
    }

}
