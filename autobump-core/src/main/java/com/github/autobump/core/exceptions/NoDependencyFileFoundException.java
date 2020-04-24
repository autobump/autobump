package com.github.autobump.core.exceptions;

public class NoDependencyFileFoundException extends RuntimeException {

    private static final long serialVersionUID = 3426608271051128854L;

    public NoDependencyFileFoundException(String s, Exception cause) {
        super(s, cause);
    }
}
