package com.github.autobump.maven.exceptions;

public class DependencyNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 8611954154370624230L;

    public DependencyNotFoundException(String message) {
        super(message);
    }
}
