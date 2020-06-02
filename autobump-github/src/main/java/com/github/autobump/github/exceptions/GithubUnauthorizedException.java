package com.github.autobump.github.exceptions;

public class GithubUnauthorizedException extends RuntimeException {
    private static final long serialVersionUID = 4235743755689632833L;

    public GithubUnauthorizedException(String message) {
        super(message);
    }
}
