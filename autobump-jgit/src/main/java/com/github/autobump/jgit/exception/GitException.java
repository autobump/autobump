package com.github.autobump.jgit.exception;

public class GitException extends RuntimeException {

    public GitException(String message, Throwable cause) {
        super(message, cause);
    }
}
