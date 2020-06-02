package com.github.autobump.bitbucket.exceptions;

public class BitbucketApiException extends RuntimeException {
    private static final long serialVersionUID = 299957257775157236L;

    public BitbucketApiException(String message) {
        super(message);
    }
}
