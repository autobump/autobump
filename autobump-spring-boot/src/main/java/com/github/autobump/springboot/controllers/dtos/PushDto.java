package com.github.autobump.springboot.controllers.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class PushDto {
    Data data;

    public PushDto(String branchname) {
        this.data = new Data(branchname);
    }

    public String getBranchName(){
        return data.getBranchName();
    };
    @lombok.Data
    public static class Data {
        Push push;

        public Data(String branchname) {
            this.push = new Push(branchname);
        }

        public String getBranchName(){
            return push.getBranchName();
        }
        @lombok.Data
        public static class Push {
            List<Change> changes;

            public Push(String branchname) {
                this.changes = List.of(new Change(branchname));
            }

            public String getBranchName() {
                return changes.stream().map(Change::getBranchName).findAny().get();
            }

            @lombok.Data
            public static class Change {
                @JsonProperty("new")
                Event event;

                public Change(String branchname) {
                    this.event = new Event(branchname);
                }

                public String getBranchName() {
                    return event.name;
                }

                @lombok.Data
                public static class Event {
                    String name;

                    public Event(String branchname) {
                        this.name = branchname;
                    }
                }
            }
        }
    }
}
