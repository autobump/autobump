package com.github.autobump.springboot.configuration;

import com.github.autobump.bitbucket.model.BitBucketGitProvider;
import com.github.autobump.core.model.GitClient;
import com.github.autobump.github.model.GithubReleaseNotesSource;
import com.github.autobump.maven.model.MavenVersionRepository;
import com.github.autobump.springboot.interceptors.JwtInterceptor;
import com.github.autobump.springboot.services.JwtFactory;
import com.github.autobump.springboot.services.SpringbootJGitGitClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Autobumpconfig {

    @Value("${autobump.bitbucket.apiUrl}")
    private String bitbucketApiUrl;
    @Value("${autobump.maven.pubRepoUrl}")
    private String mavenRepositoryUrl;
    @Value("${autobump.github.apiUrl}")
    private String githubApiUrl;

    @Bean
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    public GitClient gitClient(JwtFactory jwtFactory) {
        return new SpringbootJGitGitClient(jwtFactory);
    }

    @Bean
    public MavenVersionRepository mavenVersionRepository() {
        return new MavenVersionRepository(mavenRepositoryUrl);
    }

    @Bean
    public GithubReleaseNotesSource githubReleaseNotesSource() {
        return new GithubReleaseNotesSource(githubApiUrl);
    }

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
