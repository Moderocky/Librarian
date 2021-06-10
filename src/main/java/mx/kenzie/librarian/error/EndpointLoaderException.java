package mx.kenzie.librarian.error;

public class EndpointLoaderException extends RuntimeException {
    public EndpointLoaderException() {
        super();
    }
    
    public EndpointLoaderException(String message) {
        super(message);
    }
    
    public EndpointLoaderException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public EndpointLoaderException(Throwable cause) {
        super(cause);
    }
    
    protected EndpointLoaderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
