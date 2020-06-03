package com.github.autobump.springboot.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="Branches")
@Getter
@Setter
@NoArgsConstructor
public class Branch {
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    long branchId;
    String name;
    boolean isTarget;
    @ManyToOne
    @JoinColumn(name="repositoryId")
    Repository repository;
}
