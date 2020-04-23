package exceptions;

public class NoDependencyFileFoundException extends RuntimeException {
    private static final long serialVersionUID = 4L;

    public NoDependencyFileFoundException(String s, Exception cause) {
        super(s, cause);
    }
}
