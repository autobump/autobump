package com.github.autobump.core.model;

import com.github.autobump.core.exceptions.NoDependencyFileFoundException;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

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

    public void walkFiles(Set<Dependency> dependencies,
                          Set<Dependency> toBeIgnored,
                          DependencyResolver dependencyResolver) {
        try {
            Files.walkFileTree(Path.of(getProjectRoot()),
                    Set.of(),
                    2, new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                            String fileName = dependencyResolver.getBuildFileName();
                            if (!file.toString().equals(getProjectRoot() + File.separator + fileName) &&
                                    file.getFileName().toString().equals(fileName)) {
                                Workspace ws = new Workspace(file
                                        .toAbsolutePath()
                                        .toString()
                                        .replace(File.separator + fileName, ""));
                                dependencies.addAll(dependencyResolver.resolve(ws, toBeIgnored));
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            throw new UncheckedIOException("the files could not be processed", e);
        }
    }

}
