package com.github.autobump.github.model;

import com.github.autobump.github.model.dtos.VersionInformationDto;
import feign.Param;
import feign.RequestLine;

import java.util.Set;

interface GithubApi {
    @RequestLine("GET /repos/{repoOwner}/{repoName}/releases")
    Set<VersionInformationDto> getAllReleaseNotes(@Param("repoOwner") String repoOwner,
                                                  @Param("repoName") String repoName);
}
