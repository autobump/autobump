package com.github.autobump.bitbucket.model.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class RepositoryResponseDto {
    private int pagelen;
    private List<Repository> values;
    private String next;

    public List<String> getCloneLinks() {
        return values.stream()
                .flatMap(repository -> repository
                        .getCloneLink()
                        .stream())
                .collect(Collectors.toUnmodifiableList());
    }

    @Data
    private static class Repository {
        Links links;

        public List<String> getCloneLink() {
            return links.getCloneLink();
        }

        @Data
        private static class Links{
            private List<Link> clone;

            public List<String> getCloneLink() {
                List<String> cloneLinks = new ArrayList<>();
                for (Link link : clone) {
                    if ("https".equalsIgnoreCase(link.name)){
                        cloneLinks.add(link.href);
                    }
                }
                return cloneLinks;
            }

            @Data
            private static class Link{
                private String href;
                private String name;
            }
        }
    }
}
