package com.github.autobump.springboot.controllers.converters;

import com.github.autobump.springboot.controllers.dtos.DependencyDto;
import com.github.autobump.springboot.services.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DependencyConverter implements Converter<String, DependencyDto> {

    @Autowired
    SettingsService service;

    @Override
    public DependencyDto convert(String id) {

        int parsedId = Integer.parseInt(id);
        List<DependencyDto> selectableDependencies = new ArrayList<>();
        for (DependencyDto dto : service.getAllDependenciesFromRepo()
        ) {
            selectableDependencies.add(dto);
        }
        int index = parsedId - 1;
        return selectableDependencies.get(index);
    }
}
