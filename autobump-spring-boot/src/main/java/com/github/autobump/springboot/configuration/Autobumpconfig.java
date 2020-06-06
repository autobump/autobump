package com.github.autobump.springboot.configuration;

import com.atlassian.connect.spring.AtlassianHostRepository;
import com.github.autobump.bitbucket.model.BitBucketGitProvider;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.github.model.GithubReleaseNotesSource;
import com.github.autobump.maven.model.MavenVersionRepository;
import com.github.autobump.springboot.interceptors.JwtInterceptor;
import com.github.autobump.springboot.services.JwtFactory;
import com.github.autobump.springboot.services.SpringbootJGitGitClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Autobumpconfig {

//    @Autowired
//    private RestTemplateBuilder restTemplateBuilder;
//    @Autowired
//    private AtlassianHostRepository repository;
//    @Value("${autobump.bitbucket.oAuthUrl}")
//    private String oAuthUrl;
//    @Value("$autobump.bitbucket.key")
//    private String appKey;
    @Value("${autobump.bitbucket.apiUrl}")
    private String bitbucketApiUrl;
    @Value("${autobump.maven.pubRepoUrl}")
    private String mavenRepositoryUrl;
    @Value("${autobump.github.apiUrl}")
    private String githubApiUrl;


//    public UseCaseConfiguration setupConfig() {
//        return UseCaseConfiguration.builder()
//                .dependencyBumper(getMavenDependencyBumper())
//                .dependencyResolver(getDependencyResolver())
//                .gitClient(getGitClient())
//                .gitProvider(getGitProvider())
//                .ignoreRepository(getIgnoreRepo())
//                .gitProviderUrlHelper(getUrlHelper())
//                .versionRepository(getVersionRepository())
//                .build();
//    }
//
//    public DependencyBumper getMavenDependencyBumper() {
//        return new MavenDependencyBumper();
//    }
//
//    public DependencyResolver getDependencyResolver() {
//        return new MavenDependencyResolver(new MavenModelAnalyser());
//    }

//    public IgnoreRepository getIgnoreRepo() {
//        return new MavenIgnoreRepository(Collections.emptyMap());
//    }
//
//    public GitProviderUrlHelper getUrlHelper() {
//        return new BitBucketGitProviderUrlHelper();
//    }
//
//    public VersionRepository getVersionRepository() {
//        return new MavenVersionRepository();
//    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public GitClient gitClient(JwtFactory jwtFactory) {
//        return new JGitGitClient(properties.getUsername(), properties.getPassword());
//        return new JGitGitClient("x-token-auth", getAccesToken());
        return new SpringbootJGitGitClient(jwtFactory);
    }

//    public String getAccesToken() {
//        var jwt = getJwt();
//        var headers = new HttpHeaders();
//        headers.set(HttpHeaders.AUTHORIZATION, "JWT " + jwt);
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        var map = new LinkedMultiValueMap<String, String>();
//        map.add("grant_type", "urn:bitbucket:oauth2:jwt");
//        var entity = new HttpEntity<>(map, headers);
//        return restTemplateBuilder.build().postForObject(oAuthUrl,
//                entity,
//                AccessTokenDto.class)
//                .getToken();
//    }
//
//    public String getJwt(){
//        AtlassianHost host = null;
//        for (AtlassianHost atlassianHost : repository.findAll()) {
//            host = atlassianHost;
//        }
//
//        if (host != null) {
//            return new JwtBuilder()
//                    .subject(host.getClientKey()) // = client key (retrieved on /install)
//                    .issuer(appKey) // = app key
//                    .signature(host.getSharedSecret()) // = shared secret (retrieved on /install)
//                    .build();
//        }
//        return null;
//    }

    @Bean
    public MavenVersionRepository mavenVersionRepository() {
        return new MavenVersionRepository(mavenRepositoryUrl);
    }


//    public GitClient getGitClient() {
//        return new JGitGitClient("x-token-auth", getAccesToken());
//    }
//

    @Bean
    public GithubReleaseNotesSource githubReleaseNotesSource() {
        return new GithubReleaseNotesSource(githubApiUrl);
    }

    //    public GitProvider getGitProvider() {
//        return new BitBucketGitProvider(new JwtInterceptor(getJwt()));
//    }

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public BitBucketGitProvider bitBucketGitProvider(JwtInterceptor jwtInterceptor) {
        return new BitBucketGitProvider(bitbucketApiUrl, jwtInterceptor);
    }

    @Bean
    public JwtInterceptor jwtInterceptor(JwtFactory jwtFactory){
        return new JwtInterceptor(jwtFactory);
    }
}
