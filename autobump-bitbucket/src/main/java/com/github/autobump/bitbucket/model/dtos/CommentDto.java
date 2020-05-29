package com.github.autobump.bitbucket.model.dtos;

import lombok.Value;

@Value
public class CommentDto {
    Content content;

    @Value
    public static class Content{
        String raw;
    }


}
