package com.github.autobump.bitbucket.exceptions;

public class BitbucketNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1041735832042109576L;

    public BitbucketNotFoundException(String message) {
        super(message);
    }
}
