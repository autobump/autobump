package exception;

public class BranchNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -3647841711404594762L;

    public BranchNotFoundException(String message) {
        super(message);
    }
}
