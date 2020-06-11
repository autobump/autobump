package com.github.autobump.github.model;

import com.github.autobump.core.model.domain.ReleaseNotes;
import com.github.autobump.core.model.releasenotes.ReleaseNotesSource;
import com.github.autobump.core.model.releasenotes.ReleaseNotesUrlHelper;
import com.github.autobump.github.exceptions.GithubNotFoundException;
import feign.Feign;
import feign.jackson.JacksonDecoder;


public class GithubReleaseNotesSource implements ReleaseNotesSource {

    private static final String INVISIBLE_SPACE = "\u200B";

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
        try {
            return githubApi.getAllReleaseNotes(ownerName, repoName).stream()
                    .filter(versionInformationDto -> versionInformationDto.getTagName().endsWith(versionNumber))
                    .map(versionInformationDto ->
                            new ReleaseNotes(versionInformationDto.getHtmlUrl(),
                                    versionInformationDto.getTagName(),
                                    removeUsernameMentionsFromBody(versionInformationDto.getBody())))
                    .findFirst()
                    .orElse(null);
        } catch (GithubNotFoundException g) {
            return null;
        }
    }

    private String removeUsernameMentionsFromBody(String body) {
        return body.replace("@", "@" + INVISIBLE_SPACE);
    }
}
