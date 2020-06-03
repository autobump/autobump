package com.github.autobump.springboot.controllers.converters;

import com.github.autobump.springboot.controllers.dtos.BranchDto;
import com.github.autobump.springboot.services.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BranchConverter implements Converter<String, BranchDto> {
    @Autowired
    SettingsService service;

    @Override
    public BranchDto convert(String id) {

        int parsedId = Integer.parseInt(id);
        List<BranchDto> selectableBranches = new ArrayList<>();
        for (BranchDto dto : service.getAllBranchesFromRepo()
        ) {
            selectableBranches.add(dto);
        }
        int index = parsedId - 1;
        return selectableBranches.get(index);
    }
}
