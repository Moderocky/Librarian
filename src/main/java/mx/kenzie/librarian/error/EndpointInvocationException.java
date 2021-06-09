package mx.kenzie.librarian.error;

public class EndpointInvocationException extends RuntimeException {
    
    public EndpointInvocationException() {
        super();
    }
    
    public EndpointInvocationException(String message) {
        super(message);
    }
    
    public EndpointInvocationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public EndpointInvocationException(Throwable cause) {
        super(cause);
    }
    
    protected EndpointInvocationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
