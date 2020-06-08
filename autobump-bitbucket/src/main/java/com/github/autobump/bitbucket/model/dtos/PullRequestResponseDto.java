package com.github.autobump.bitbucket.model.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class PullRequestResponseDto {
    String type;
    String description;
    Map<String, Link> links;
    String title;
    int id;
    String state;
    @JsonProperty(value = "comment_count")
    int commentCount;

    public String getLink() {
        return links.get("html").getHref();
    }

    @Data
    static class Link {
        String href;
    }
}
