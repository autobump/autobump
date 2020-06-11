package com.github.autobump.core.model.domain;

import com.github.autobump.core.exceptions.NoDependencyFileFoundException;
import lombok.Getter;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

@Getter
public class Workspace {
    private final String projectRoot;

    public Workspace(String projectRoot) {
        this.projectRoot = projectRoot;
    }

    public Reader getDependencyDocument(String fileName) {
        try {
            return Files.newBufferedReader(Paths.get(projectRoot, fileName));
        } catch (IOException e) {
            throw new NoDependencyFileFoundException("Reader could not load dependency file", e);
        }
    }

}
