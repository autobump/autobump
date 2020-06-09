package com.github.autobump.bitbucket.model.dtos;

import lombok.Value;

import java.util.List;

@Value
public class PullRequestBodyDto {
    String title;
    PullRequestBodyDto.Source source;
    List<Reviewer> reviewers;

    @Value
    public static class Source{
        PullRequestBodyDto.Branch branch;
    }

    @Value
    public static class Reviewer{
        String uuid;
    }

    @Value
    public static class Branch{
        String name;
    }
}
