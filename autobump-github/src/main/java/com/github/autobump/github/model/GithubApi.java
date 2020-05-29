package com.github.autobump.github.model;

import com.github.autobump.github.model.dtos.VersionInformationDto;
import feign.Param;
import feign.RequestLine;

interface GithubApi {
    @RequestLine("GET /repos/{repoOwner}/{repoName}/releases/tags/v{versionNumber}")
    VersionInformationDto getReleaseNotes(@Param("repoOwner") String repoOwner,
                                          @Param("repoName") String repoName,
                                          @Param("versionNumber") String versionNumber);
}
