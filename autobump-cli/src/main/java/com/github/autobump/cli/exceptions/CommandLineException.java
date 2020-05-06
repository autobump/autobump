package com.github.autobump.cli.exceptions;

public class CommandLineException extends RuntimeException {
    private static final long serialVersionUID = 1234217057663497798L;

    public CommandLineException(String message) {
        super(message);
    }
}
