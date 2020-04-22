package exceptions;

public class DependencyParserException extends RuntimeException{

    public DependencyParserException(String s, Exception cause){
        super(s, cause);
    }
}
