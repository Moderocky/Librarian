package mx.kenzie.librarian.error;

public class RegistryAccessException extends RuntimeException {
    public RegistryAccessException() {
        super();
    }
    
    public RegistryAccessException(String message) {
        super(message);
    }
    
    public RegistryAccessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public RegistryAccessException(Throwable cause) {
        super(cause);
    }
    
    protected RegistryAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
