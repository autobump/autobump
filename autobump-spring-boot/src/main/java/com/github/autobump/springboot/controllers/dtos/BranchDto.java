package com.github.autobump.springboot.controllers.dtos;

import lombok.Value;

@Value
public class BranchDto {
    int id;
    String name;
    boolean isTarget;
}
