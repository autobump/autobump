package com.github.autobump.github.exceptions;

public class GithubNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1041735832042109576L;

    public GithubNotFoundException(String message) {
        super(message);
    }
}
