package com.github.autobump.springboot.controllers.dtos;

import lombok.Data;

@Data
public class RejectedDto {
    Data data;
    public String getPrTitle(){
        return data.getPrTitle();
    }

    public String getRepoName(){
        return data.getRepoName();
    }
    @lombok.Data
    static class Data{
        PullRequest pullrequest;
        Repository repository;
        public String getPrTitle() {
            return pullrequest.getTitle();
        }

        public String getRepoName() {
            return repository.getName();
        }

        @lombok.Data
        static class PullRequest{
            String title;
        }

        @lombok.Data
        static class Repository{
            String name;
        }
    }
}
