package com.github.autobump.springboot.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class RepositoryDto {
    private Boolean selected;
    String name;
    int id;
    // to add: link?

}
