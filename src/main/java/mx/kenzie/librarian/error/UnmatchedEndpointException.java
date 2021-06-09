package mx.kenzie.librarian.error;

public class UnmatchedEndpointException extends RuntimeException {
    
    public UnmatchedEndpointException() {
        super();
    }
    
    public UnmatchedEndpointException(String message) {
        super(message);
    }
    
    public UnmatchedEndpointException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public UnmatchedEndpointException(Throwable cause) {
        super(cause);
    }
    
    protected UnmatchedEndpointException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
