package com.github.autobump.maven.exceptions;

public class WrongUrlException extends RuntimeException {

    private static final long serialVersionUID = 3231597597844341347L;

    public WrongUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}
