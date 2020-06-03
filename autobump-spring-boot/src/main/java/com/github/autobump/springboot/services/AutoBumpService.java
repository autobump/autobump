package com.github.autobump.springboot.services;

import com.atlassian.connect.spring.AtlassianHostRepository;
import com.atlassian.connect.spring.AtlassianHostRestClients;
import com.github.autobump.core.model.AutobumpResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AutoBumpService {
    private final static Long DAY_IN_MS = 86400000L;
    @Autowired
    AtlassianHostRepository repository;

    @Autowired
    AtlassianHostRestClients restClients;

    @Scheduled(fixedRate = DAY_IN_MS)
    private List<AutobumpResult> autoBump(){
        List<AutobumpResult> results = new ArrayList<>();
        var repos = restClients.authenticatedAsAddon()
                .getForObject("https://api.bitbucket.org/2.0/repositories", String.class);
        System.out.println("repos = " + repos);
        return results;
    }
}
