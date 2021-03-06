package com.github.autobump.bitbucket.model;

import com.github.autobump.bitbucket.model.dtos.CommentDto;
import com.github.autobump.bitbucket.model.dtos.PullRequestBodyDto;
import com.github.autobump.bitbucket.model.dtos.PullRequestResponseDto;
import com.github.autobump.core.model.domain.PullRequest;
import com.github.autobump.core.model.domain.Repo;
import com.github.autobump.core.model.gitproviders.GitProvider;
import com.github.autobump.core.model.gitproviders.GitProviderUrlHelper;
import com.github.autobump.core.model.results.PullRequestResult;
import feign.Feign;
import feign.RequestInterceptor;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class BitBucketGitProvider implements GitProvider {
    private final String apiUrl;
    private final BitBucketApi client;
    private final GitProviderUrlHelper bitBucketGitProviderUrlHelper;

    public BitBucketGitProvider(BitBucketAccount user){
        this(user, "https://api.bitbucket.org/2.0");
    }
    public BitBucketGitProvider(RequestInterceptor interceptor){
        this("https://api.bitbucket.org/2.0", interceptor);
    }

    public BitBucketGitProvider(BitBucketAccount user, String apiUrl) {
        this(apiUrl, new BasicAuthRequestInterceptor(user.getUsername(), user.getPassword()));
    }

    public BitBucketGitProvider(String apiUrl, RequestInterceptor interceptor){
        this.apiUrl = apiUrl;
        client = Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .requestInterceptor(interceptor)
                .errorDecoder(new BitBucketErrorDecoder())
                .target(BitBucketApi.class, apiUrl);
        bitBucketGitProviderUrlHelper = new BitBucketGitProviderUrlHelper();
    }

    @Override
    public PullRequestResult makePullRequest(PullRequest pullRequest) {
        PullRequestBodyDto body = getPullRequestBodyDto(pullRequest);
        PullRequestResponseDto dto
                = client.createPullRequest(pullRequest.getRepoOwner(), pullRequest.getRepoName(), body);
        return PullRequestResult.builder()
                .type(dto.getType())
                .description(dto.getDescription())
                .link(dto.getLink())
                .title(dto.getTitle())
                .id(dto.getId())
                .state(dto.getState())
                .commentCount(dto.getCommentCount())
                .build();
    }

    private PullRequestBodyDto getPullRequestBodyDto(PullRequest pullRequest) {
        if (pullRequest.getReviewer() != null){
            return new PullRequestBodyDto(pullRequest.getTitle(),
                    new PullRequestBodyDto.Source(new PullRequestBodyDto.Branch(pullRequest.getBranchName())),
                    List.of(new PullRequestBodyDto.Reviewer(pullRequest.getReviewer())));
        }
        else {
            return new PullRequestBodyDto(pullRequest.getTitle(),
                    new PullRequestBodyDto.Source(new PullRequestBodyDto.Branch(pullRequest.getBranchName())));
        }
    }

    @Override
    public Set<PullRequest> getOpenPullRequests(String repoOwner, String repoName) {
        return client.getOpenPullRequests(repoOwner, repoName)
                .getValues()
                .stream().filter(p -> p.getTitle().startsWith("Bumped"))
                .map(d -> PullRequest.builder()
                .pullRequestId(bitBucketGitProviderUrlHelper.getPullRequestId(d.getLink()))
                .repoName(repoName)
                .repoOwner(repoOwner)
                .title(d.getTitle())
                .branchName(parseBranchName(d))
                .build())
                .collect(Collectors.toUnmodifiableSet());
    }

    private String parseBranchName(PullRequestResponseDto dto) {
        String[] elements = dto.getTitle().split(" ");
        String groupId = elements[1].split(":")[0];
        String version = elements[elements.length - 1];
        return String.format("autobump/%s/%s", groupId, version);
    }

    @Override
    public void closePullRequest(PullRequest pullRequest) {
        client.closePullRequest(pullRequest.getRepoOwner(),
                pullRequest.getRepoName(),
                String.valueOf(pullRequest.getPullRequestId()));
    }

    @Override
    public void commentPullRequest(PullRequest pr, String comment) {
        CommentDto dto = new CommentDto(new CommentDto.Content(comment));
        client.commentPullRequest(pr.getRepoOwner(),
                pr.getRepoName(),
                String.valueOf(pr.getPullRequestId()),
                dto);
    }

    @Override
    public List<Repo> getRepos() {
        return client.getRepos()
                .getValues()
                .stream()
                .map(r -> new Repo(r.getUuid(), r.getCloneLink(), r.getName()))
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Map<String, String> getMembersFromWorkspace(Repo repo){
        String workspaceName = bitBucketGitProviderUrlHelper.getOwnerName(repo.getLink());
        return client.getMembersFromWorkspace(workspaceName)
                .getMembers();
    }

    @Override
    public String getCurrentUserUuid() {
        return client.getCurrentUserUuid().getUuid();
    }

}
