package com.github.autobump.springboot.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name="Repositories")
@Getter
@Setter
@NoArgsConstructor
public class Repository {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    long repositoryId;
    boolean selected;
    String name;
    /*@ManyToOne
    List<Dependency> dependencies;*/
    boolean cronJob;
    String reviewer;
    @OneToMany(mappedBy = "repository")
    List<Branch> branches;
    boolean ignore;
}
