package mx.kenzie.librarian;

import mx.kenzie.librarian.error.EndpointInvocationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

record Endpoint(Object resource, Method method) {
    
    public Object invoke(final Object... parameters)
        throws EndpointInvocationException {
        try {
            return method.invoke(resource, parameters);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new EndpointInvocationException(ex);
        }
    }
    
}
