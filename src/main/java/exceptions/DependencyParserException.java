package exceptions;

public class DependencyParserException extends RuntimeException{
    private static final long serialVersionUID = 4L;

    public DependencyParserException(String s, Exception cause){
        super(s, cause);
    }
}
