package com.github.autobump.bitbucket.model.dtos;

import lombok.Data;

import java.util.List;

@Data
public class PullRequestListDto {
    List<PullRequestDto> pullRequestDtoList;
}
