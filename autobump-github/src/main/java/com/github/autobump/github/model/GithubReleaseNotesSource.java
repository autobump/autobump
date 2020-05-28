package com.github.autobump.github.model;

import com.github.autobump.core.model.ReleaseNotesSource;
import com.github.autobump.core.model.ReleaseNotesUrlHelper;
import feign.Feign;
import feign.jackson.JacksonDecoder;

public class GithubReleaseNotesSource implements ReleaseNotesSource {
    private final GithubApi githubApi;
    private final ReleaseNotesUrlHelper releaseNotesUrlHelper;

    public GithubReleaseNotesSource() {
        this("https://api.github.com");
    }

    public GithubReleaseNotesSource(String apiUrl) {
        this.githubApi = Feign.builder()
                .decoder(new JacksonDecoder())
                .errorDecoder(new GithubErrorDecoder())
                .target(GithubApi.class, apiUrl);
        this.releaseNotesUrlHelper = new GithubUrlHelper();
    }

    @Override
    public String getReleaseNotes(String projectUrl, String versionNumber) {
        return githubApi.getReleaseNotes(releaseNotesUrlHelper.getOwnerName(projectUrl),
                releaseNotesUrlHelper.getRepoName(projectUrl),
                versionNumber)
                .getBody();
    }
}
