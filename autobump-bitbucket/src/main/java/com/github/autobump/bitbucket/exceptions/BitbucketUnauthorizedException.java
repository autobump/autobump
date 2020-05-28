package com.github.autobump.bitbucket.exceptions;

public class BitbucketUnauthorizedException extends RuntimeException {
    private static final long serialVersionUID = 4235743755689632833L;

    public BitbucketUnauthorizedException(String message) {
        super(message);
    }
}
