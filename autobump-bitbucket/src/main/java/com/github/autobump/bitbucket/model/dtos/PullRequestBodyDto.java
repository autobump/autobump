package com.github.autobump.bitbucket.model.dtos;

import lombok.Value;

@Value
public class PullRequestBodyDto {
    String title;
    PullRequestBodyDto.Source source;

    @Value
    public static class Source{
        PullRequestBodyDto.Branch branch;
    }

    @Value
    public static class Branch{
        String name;
    }
}
