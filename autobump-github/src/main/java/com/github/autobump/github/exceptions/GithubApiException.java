package com.github.autobump.github.exceptions;

public class GithubApiException extends RuntimeException {
    private static final long serialVersionUID = 299957257775157236L;

    public GithubApiException(String message) {
        super(message);
    }
}
