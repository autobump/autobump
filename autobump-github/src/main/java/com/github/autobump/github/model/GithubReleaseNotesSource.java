package com.github.autobump.github.model;

import com.github.autobump.core.model.ReleaseNotesSource;
import com.github.autobump.core.model.ReleaseNotesUrlHelper;
import com.github.autobump.github.model.dtos.VersionInformationDto;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        StringBuilder releaseNotes = new StringBuilder();
        String ownerName = releaseNotesUrlHelper.getOwnerName(projectUrl);
        String repoName = releaseNotesUrlHelper.getRepoName(projectUrl);
        githubApi.getAllReleaseNotes(ownerName, repoName).stream()
                .filter(versionInformationDto -> versionInformationDto.getTagName().endsWith(versionNumber))
                .map(VersionInformationDto::getTagName)
                .findFirst()
                .ifPresent(versionTag ->
                        releaseNotes.append(githubApi.getReleaseNotes(ownerName, repoName, versionTag).getBody()));
        return releaseNotes.toString();
    }
}