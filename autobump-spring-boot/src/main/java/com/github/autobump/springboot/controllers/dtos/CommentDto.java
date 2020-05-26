package com.github.autobump.springboot.controllers.dtos;

import lombok.Data;

@Data
public class CommentDto {
    private Data data;

    public String getCommment() {
        return data.getCommentText();
    }

    public String getPrTitle() {
        return data.getPrTitle();
    }

    public String getRepoName(){
        return data.getRepoName();
    }

    @lombok.Data
    static class Data {
        Comment comment;
        Repository repository;

        public String getCommentText() {
            return comment.getCommentText();
        }

        public String getPrTitle() {
            return comment.getPrTitle();
        }
        public String getRepoName(){
            return repository.getName();
        }

        @lombok.Data
        static class Comment {
            Content content;
            PullRequest pullrequest;

            public String getCommentText() {
                return content.getRaw();
            }

            public String getPrTitle() {
                return pullrequest.getTitle();
            }

            @lombok.Data
            static class Content {
                String raw;
            }

            @lombok.Data
            static class PullRequest {
                String title;
            }
        }

        @lombok.Data
        static class Repository{
            String name;
        }
    }
}
