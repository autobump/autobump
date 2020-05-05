package com.github.autobump.bitbucket.exceptions;

public class RemoteNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1041735832042109576L;

    public RemoteNotFoundException(String message) {
        super(message);
    }
}
