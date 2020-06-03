package com.github.autobump.springboot.controllers.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RepositoryListDto {
    List<RepositoryDto> repositories;
}
