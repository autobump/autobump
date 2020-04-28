package com.github.autobump.core.exceptions;

public class DependencyParserException extends RuntimeException{

    private static final long serialVersionUID = -1062390353272772788L;

    public DependencyParserException(String s, Exception cause){
        super(s, cause);
    }

    public DependencyParserException(String s) {
        super(s);
    }
}
