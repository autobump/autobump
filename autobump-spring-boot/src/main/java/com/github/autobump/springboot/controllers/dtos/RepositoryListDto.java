package com.github.autobump.springboot.controllers.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RepositoryListDto {
    List<RepositoryDto> repositories;

    public RepositoryListDto() {
        this.repositories = new ArrayList<>();
    }
}
