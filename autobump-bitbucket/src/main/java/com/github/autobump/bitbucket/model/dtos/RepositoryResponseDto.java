package com.github.autobump.bitbucket.model.dtos;

import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Getter
public class RepositoryResponseDto {
    private int pagelen;
    private List<Repository> values;
    private String next;

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
                String ref = "";
                for (Link link : clone) {
                    if ("https".equalsIgnoreCase(link.name)){
                        ref = link.href;
                    }
                }
                return ref;
            }

            @Data
            private static class Link{
                private String href;
                private String name;
            }
        }
    }
}
