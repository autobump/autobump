package exceptions;

public class NoDependencyFileFoundException extends RuntimeException {
    public NoDependencyFileFoundException(String s, Exception cause) {
        super(s, cause);
    }
}
