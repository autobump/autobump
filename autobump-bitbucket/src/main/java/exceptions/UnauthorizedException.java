package exceptions;

public class UnauthorizedException extends RuntimeException {
    private static final long serialVersionUID = 4235743755689632833L;

    public UnauthorizedException(String message) {
        super(message);
    }
}
