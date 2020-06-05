package com.github.autobump.bitbucket.model.dtos;

import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Getter
public class RepositoryResponseDto {
    private int pagelen;
    private List<Repository> values;
    private String next;

    public List<String> getCloneLinks() {
        return values.stream()
                .map(repository -> repository
                        .getCloneLink())
                .collect(Collectors.toUnmodifiableList());
    }

    @Data
    @Getter
    public static class Repository {
        Links links;
        String uuid;
        String name;

        public String getCloneLink() {
            return links.getCloneLink();
        }

        @Data
        private static class Links{
            private List<Link> clone;

            public String getCloneLink() {
                List<String> cloneLinks = new ArrayList<>();
                for (Link link : clone) {
                    if ("https".equalsIgnoreCase(link.name)){
                        return link.href;
                    }
                }
                return null;
            }

            @Data
            private static class Link{
                private String href;
                private String name;
            }
        }
    }
}
