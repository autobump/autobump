package com.github.autobump.springboot.controllers.dtos;

import lombok.Data;

@Data
public class CommentDto {
    private Data data;
    public CommentDto(String comment, String reponame, String prTitle){
        this.data = new Data(comment, reponame, prTitle);
    }

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

        Data(String comment, String reponame, String prTitle) {
            this.comment = new Comment(comment, prTitle);
            this.repository = new Repository(reponame);
        }

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

            Comment(String comment, String prTitle) {
                this.content = new Content(comment);
                this.pullrequest = new PullRequest(prTitle);
            }

            public String getCommentText() {
                return content.getRaw();
            }

            public String getPrTitle() {
                return pullrequest.getTitle();
            }

            @lombok.Data
            static class Content {
                String raw;

                Content(String comment) {
                    this.raw = comment;
                }
            }

            @lombok.Data
            static class PullRequest {
                String title;

                PullRequest(String prTitle) {
                    this.title = prTitle;
                }
            }
        }

        @lombok.Data
        static class Repository{
            String name;

            Repository(String reponame) {
                this.name = reponame;
            }
        }
    }
}
