package com.github.autobump.github.model;

import com.github.autobump.core.model.ReleaseNotesSource;
import com.github.autobump.core.model.ReleaseNotesUrlHelper;
import com.github.autobump.core.model.usecases.ReleaseNotes;
import feign.Feign;
import feign.jackson.JacksonDecoder;


public class GithubReleaseNotesSource implements ReleaseNotesSource {
    private final GithubApi githubApi;
    private final ReleaseNotesUrlHelper releaseNotesUrlHelper;

    public GithubReleaseNotesSource(String apiUrl) {
        this.githubApi = Feign.builder()
                .decoder(new JacksonDecoder())
                .errorDecoder(new GithubErrorDecoder())
                .target(GithubApi.class, apiUrl);
        this.releaseNotesUrlHelper = new GithubUrlHelper();
    }

    @Override
    public ReleaseNotes getReleaseNotes(String projectUrl, String versionNumber) {
        String ownerName = releaseNotesUrlHelper.getOwnerName(projectUrl);
        String repoName = releaseNotesUrlHelper.getRepoName(projectUrl);
        return githubApi.getAllReleaseNotes(ownerName, repoName).stream()
                .filter(versionInformationDto -> versionInformationDto.getTagName().endsWith(versionNumber))
                .map(versionInformationDto ->
                        new ReleaseNotes(versionInformationDto.getHtmlUrl(),
                                versionInformationDto.getTagName(),
                                versionInformationDto.getBody()))
                .findFirst()
                .orElse(null);
    }
}