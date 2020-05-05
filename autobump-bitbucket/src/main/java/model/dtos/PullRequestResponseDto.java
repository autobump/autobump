package model.dtos;

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

    public String getLink() {
        return links.get("html").getHref();
    }

    @Data
    static class Link {
        String href;
    }
}
