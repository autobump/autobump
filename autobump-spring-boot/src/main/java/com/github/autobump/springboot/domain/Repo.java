package com.github.autobump.springboot.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="repos")
@Data
public class Repo {
    @Id
    int repoId;
    boolean selected;
    String name;
    boolean cronJob;
    String reviewer;
    String link;
}

