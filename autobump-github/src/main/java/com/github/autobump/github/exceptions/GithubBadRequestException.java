package com.github.autobump.github.exceptions;

public class GithubBadRequestException extends RuntimeException {

    private static final long serialVersionUID = -3647841711404594762L;

    public GithubBadRequestException(String message) {
        super(message);
    }
}
