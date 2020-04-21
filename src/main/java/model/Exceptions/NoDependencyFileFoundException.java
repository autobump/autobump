package model.Exceptions;

import java.io.FileNotFoundException;

public class NoDependencyFileFoundException extends FileNotFoundException {
    public NoDependencyFileFoundException() {
    }

    public NoDependencyFileFoundException(String s) {
        super(s);
    }
}
