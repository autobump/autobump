package model;

import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.PullRequest;
import feign.Feign;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonEncoder;
import lombok.Getter;
import lombok.Setter;

import java.net.http.HttpClient;
import java.time.Duration;

@Setter
@Getter
public class BitBucketGitProvider implements GitProvider {
    private static final String API_LINK = "https://api.bitbucket.org/2.0";
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    private BitBucketAccount user;

    public BitBucketGitProvider(BitBucketAccount user) {
        this.user = user;
    }

    @Override
    public void MakePullRequest(PullRequest pullRequest) {
        PullRequestBody body = new PullRequestBody(pullRequest.getTitle(),
                new PullRequestBody.Source(new PullRequestBody.Branch(pullRequest.getBranchName())));

        BitBucketApi client = Feign.builder()
                .encoder(new JacksonEncoder())
                .requestInterceptor(new BasicAuthRequestInterceptor(user.getUsername(), user.getPassword()))
                .target(BitBucketApi.class, API_LINK);

        client.createPullRequest(pullRequest.getRepoOwner(), pullRequest.getRepoName(), body);
    }
}
