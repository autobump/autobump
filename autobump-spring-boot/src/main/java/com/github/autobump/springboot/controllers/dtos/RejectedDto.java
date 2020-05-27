package com.github.autobump.springboot.controllers.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RejectedDto {
    Data data;

    public RejectedDto(String prTitle, String reponame){
        this.data = new Data(prTitle, reponame);
    }
    public String getPrTitle(){
        return data.getPrTitle();
    }

    public String getRepoName(){
        return data.getRepoName();
    }
    @lombok.Data
    @NoArgsConstructor
    static class Data{
        PullRequest pullrequest;
        Repository repository;

        public Data(String prTitle, String reponame){
            this.pullrequest = new PullRequest();
            pullrequest.setTitle(prTitle);
            this.repository = new Repository();
            repository.setName(reponame);
        }
        public String getPrTitle() {
            return pullrequest.getTitle();
        }

        public String getRepoName() {
            return repository.getName();
        }

        @lombok.Data
        @NoArgsConstructor
        static class PullRequest{
            String title;
        }

        @lombok.Data
        @NoArgsConstructor
        static class Repository{
            String name;
        }
    }
}
