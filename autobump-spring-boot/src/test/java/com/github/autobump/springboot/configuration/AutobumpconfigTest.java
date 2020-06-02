package com.github.autobump.springboot.configuration;

import com.atlassian.connect.spring.AtlassianHost;
import com.atlassian.connect.spring.AtlassianHostRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@SpringBootTest
@ActiveProfiles("test")
class AutobumpconfigTest {

    @Autowired
    AtlassianHostRepository repository;

    @Mock
    AtlassianHostRepository hostRepository;

    WireMockServer wireMockServer;

    @Autowired
    @InjectMocks
    private Autobumpconfig autobumpconfig;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8009);
        wireMockServer.start();
        autobumpconfig.getDependencyResolver();
        var host = new AtlassianHost();
        host.setClientKey("testKey");
        host.setSharedSecret("SuperSecretkeyThatIjustToughtOfAndIsDefinitelyUnique");
        Mockito.lenient().when(hostRepository.findAll()).thenReturn(List.of(host));
        setupStubs();
    }

    private void setupStubs() {
        wireMockServer.stubFor(post(urlEqualTo("/site/oauth2/access_token"))
                .willReturn(aResponse().withBody("{\"access_token\": \"testtoken\"}")
                        .withHeader("content-type", "application/json")
                        .withStatus(200)));
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void setupConfig() {
        assertThat(autobumpconfig.setupConfig()).isNotNull();

    }

    @Test
    void getMavenDependencyBumper() {
        assertThat(autobumpconfig.getMavenDependencyBumper()).isNotNull();
    }

    @Test
    void getDependencyResolver() {
        assertThat(autobumpconfig.getDependencyResolver()).isNotNull();
    }

    @Test
    void getGitClient() {
        assertThat(autobumpconfig.getGitClient()).isNotNull();
    }

    @Test
    void getGitProvider() {
        assertThat(autobumpconfig.getGitProvider()).isNotNull();
    }

    @Test
    void getIgnoreRepo() {
        assertThat(autobumpconfig.getIgnoreRepo()).isNotNull();
    }

    @Test
    void getUrlHelper() {
        assertThat(autobumpconfig.getUrlHelper()).isNotNull();
    }

    @Test
    void getVersionRepository() {
        assertThat(autobumpconfig.getVersionRepository()).isNotNull();
    }

    @Test
    void getAccesToken() {
        assertThat(autobumpconfig.getAccesToken()).isNotNull();
    }

    @Test
    void getJwt() {
        assertThat(autobumpconfig.getJwt()).isNotNull();
    }
}
