package model.exceptions;

import java.io.FileNotFoundException;

public class NoDependencyFileFoundException extends FileNotFoundException {
    public NoDependencyFileFoundException(String s) {
        super(s);
    }
}
