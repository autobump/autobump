package com.github.autobump.jgit.exception;

public class GitException extends RuntimeException {

    private static final long serialVersionUID = -7691927742502132322L;

    public GitException(String message, Throwable cause) {
        super(message, cause);
    }
}
