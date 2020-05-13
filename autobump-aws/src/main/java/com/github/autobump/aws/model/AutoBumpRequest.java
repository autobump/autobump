package com.github.autobump.aws.model;

import lombok.Data;

import java.util.Map;

@Data
public class AutoBumpRequest {
    private String username;
    private String password;
    private String gitUrl;
    private Map<String, String> ignoreMap;
    private String repoUrl;
    private String apiUrl;

}
