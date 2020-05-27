package com.github.autobump.github.model;

import com.github.autobump.core.model.ReleaseNotesSource;
import com.github.autobump.core.model.UrlHelper;
import feign.Feign;
import feign.jackson.JacksonDecoder;

public class GithubReleaseNotesSource implements ReleaseNotesSource {
    private final GithubApi githubApi;
    private final UrlHelper urlHelper;

    public GithubReleaseNotesSource(String apiUrl, UrlHelper urlHelper) {
        this.githubApi = Feign.builder()
                .decoder(new JacksonDecoder())
                .errorDecoder(new GithubErrorDecoder())
                .target(GithubApi.class, apiUrl);
        this.urlHelper = urlHelper;
    }

    @Override
    public String getReleaseNotes(String projectUrl, String versionNumber) {
        return githubApi.getReleaseNotes(urlHelper.getOwnerName(projectUrl),urlHelper.getRepoName(projectUrl),versionNumber).getBody();
    }
}
