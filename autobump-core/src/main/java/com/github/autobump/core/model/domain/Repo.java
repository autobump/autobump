package com.github.autobump.core.model.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="repos")
@Data
@NoArgsConstructor
public class Repo {
    @Id
    String repoId;
    boolean selected;
    String name;
    String link;

    public Repo(String id, String link, String name) {
        this.repoId = id;
        this.link = link;
        this.name = name;
    }
}
