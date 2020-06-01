package com.github.autobump.springboot.controllers.dtos;

import lombok.Value;

@Value
public class RepositoryDto {
    String name;
    int id;
    // to add: link?
}
