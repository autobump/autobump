package com.github.autobump.bitbucket.model;

import com.github.autobump.bitbucket.model.dtos.PullRequestBodyDto;
import com.github.autobump.bitbucket.model.dtos.PullRequestResponseDto;
import com.github.autobump.core.model.GitProvider;
import com.github.autobump.core.model.PullRequest;
import com.github.autobump.core.model.PullRequestResponse;
import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.Getter;

import java.util.List;

@Getter
public class BitBucketGitProvider implements GitProvider {
    private final String apiUrl;
    private final BitBucketApi client;

    public BitBucketGitProvider(BitBucketAccount user, String apiUrl) {
        this.apiUrl = apiUrl;
        client = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .requestInterceptor(new BasicAuthRequestInterceptor(user.getUsername(), user.getPassword()))
                .errorDecoder(new BitBucketErrorDecoder())
                .target(BitBucketApi.class, apiUrl);
    }

    @Override
    public PullRequestResponse makePullRequest(PullRequest pullRequest) {
        PullRequestBodyDto body = new PullRequestBodyDto(pullRequest.getTitle(),
                new PullRequestBodyDto.Source(new PullRequestBodyDto.Branch(pullRequest.getBranchName())));
        PullRequestResponseDto dto
                = client.createPullRequest(pullRequest.getRepoOwner(), pullRequest.getRepoName(), body);
        return PullRequestResponse.builder()
                .type(dto.getType())
                .description(dto.getDescription())
                .link(dto.getLink())
                .title(dto.getTitle())
                .id(dto.getId())
                .state(dto.getState())
                .build();
    }

    @Override
    public List<PullRequest> getOpenPullRequests(String repoOwner, String repoName) {
        return null;
    }
}
