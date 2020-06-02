package com.github.autobump.bitbucket.exceptions;

public class BitbucketBadRequestException extends RuntimeException {

    private static final long serialVersionUID = -3647841711404594762L;

    public BitbucketBadRequestException(String message) {
        super(message);
    }
}
