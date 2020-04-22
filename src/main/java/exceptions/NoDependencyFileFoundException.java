package exceptions;

public class NoDependencyFileFoundException extends RuntimeException {
    public NoDependencyFileFoundException(String s) {
        super(s);
    }
}
