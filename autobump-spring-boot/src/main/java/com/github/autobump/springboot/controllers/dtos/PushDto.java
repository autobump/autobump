package com.github.autobump.springboot.controllers.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

@Data
public class PushDto {
    Data data;

    public String getBranchName(){
        return data.getBranchName();
    };
    @lombok.Data
    public static class Data {
        Push push;

        public String getBranchName(){
            return push.getBranchName();
        }
        @lombok.Data
        public static class Push {
            ArrayList<Change> changes;

            public String getBranchName() {
                return changes.stream().map(Change::getBranchName).findAny().get();
            }

            @lombok.Data
            public static class Change {
                @JsonProperty("new")
                Event event;

                public String getBranchName() {
                    return event.name;
                }

                @lombok.Data
                public static class Event {
                    String name;
                }
            }
        }
    }
}
