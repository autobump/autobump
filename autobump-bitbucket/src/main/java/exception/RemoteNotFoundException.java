package exception;

public class RemoteNotFoundException extends RuntimeException {
    public RemoteNotFoundException(String message) {
        super(message);
    }
}
