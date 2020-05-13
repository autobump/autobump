package com.github.autobump.aws.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AutoBumpResponse {
    private Integer numberOfBumps;
    private List<String> errors;
}
