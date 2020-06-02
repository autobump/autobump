package com.github.autobump.springboot.controllers.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PushDto {
    Data data;

    public PushDto(Data.Push.Change.Event event, String link, String uuid) {
        this.data = new Data(event, link, uuid);
    }

    public String getBranchName() {
        return data.getBranchName();
    }

    public String geturl() {
        return data.getUrl();
    }

    public String getUuid(){
        return data.getUuid();
    }

    @lombok.Data
    @NoArgsConstructor
    public static class Data {
        Push push;
        Repository repository;
        Actor actor;

        public Data(Push.Change.Event event, String link, String uuid) {
            this.push = new Push(event);
            this.repository = new Repository(link);
            this.actor = new Actor(uuid);
        }

        public String getBranchName() {
            return push.getBranchName();
        }

        public String getUrl() {
            return repository.getUrl();
        }

        public String getUuid(){
            return actor.getUuid();
        }

        @lombok.Data
        @NoArgsConstructor
        public static class Push {
            List<Change> changes;

            public Push(Change.Event event) {
                this.changes = List.of(new Change(event));
            }

            public String getBranchName() {
                for (Change change : changes) {
                    return change.getBranchName();
                }
                return null;
            }

            @lombok.Data
            @NoArgsConstructor
            public static class Change {
                @JsonProperty("new")
                Event event;

                public Change(Event event) {
                    this.event = event;
                }

                public String getBranchName() {
                    if (event!= null) {
                        return event.getName();
                    }
                    return null;
                }

                @lombok.Data
                @NoArgsConstructor
                public static class Event {
                    String name;

                    public Event(String branchname) {
                        this.name = branchname;
                    }
                }
            }
        }

        @lombok.Data
        @NoArgsConstructor
        static class Repository {
            Links links;

            Repository(String url) {
                links = new Links(url);
            }

            public String getUrl() {
                return links.getCloneUrl();
            }

            @lombok.Data
            @NoArgsConstructor
            static class Links {
                Link html;

                public Links(String url) {
                    this.html = new Link(url);
                }

                public String getCloneUrl() {
                    return html.getHref();
                }

                @lombok.Data
                @NoArgsConstructor
                static class Link {
                    String href;

                    public Link(String url) {
                        this.href = url;
                    }
                }
            }
        }

        @lombok.Data
        @NoArgsConstructor
        static class Actor{
            String uuid;

            public Actor(String uuid) {
                this.uuid = uuid;
            }
        }
    }
}
