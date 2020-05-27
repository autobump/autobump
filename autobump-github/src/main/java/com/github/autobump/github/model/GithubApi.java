package com.github.autobump.github.model;

import com.github.autobump.github.model.dtos.VersionInformationDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

interface GithubApi {
    // https://api.github.com/repos/spring-projects/spring-boot/releases/tags/v2.3.0.RELEASE
    @RequestLine("GET /repos/{repoOwner}/{repoName}/releases/tags/{versionNumber}")
    VersionInformationDto getReleaseNotes(@Param("repoOwner") String repoOwner,
                                          @Param("repoName") String repoName,
                                          @Param("versionNumber") String versionNumber);
}
