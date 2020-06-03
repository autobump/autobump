package com.github.autobump.springboot.controllers.converters;

import com.github.autobump.springboot.controllers.dtos.RepositoryDto;
import com.github.autobump.springboot.services.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RepositoryConverter implements Converter<String, RepositoryDto> {
    @Autowired
    SettingsService service;

    @Override
    public RepositoryDto convert(String id) {

        int parsedId = Integer.parseInt(id);
        List<RepositoryDto> selectableRepos = new ArrayList<>();
        for (RepositoryDto dto : service.getAllRepositoriesFromWorkspace()
        ) {
            selectableRepos.add(dto);
        }
        int index = parsedId - 1;
        return selectableRepos.get(index);
    }
}
