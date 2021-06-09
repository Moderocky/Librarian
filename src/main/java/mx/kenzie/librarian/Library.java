package mx.kenzie.librarian;

import java.lang.reflect.Method;

/**
 * A library that holds resources and handles queries for them.
 *
 * @param <Resource> The resource supertype that this library holds
 * @author Moderocky
 * @see SimpleLibrary for a basic example.
 */
public interface Library<Resource> {
    
    boolean register(Resource resource);
    
    boolean unregister(Resource resource);
    
    boolean unregisterAll(Class<? extends Resource> type);
    
    boolean unregisterAll();
    
    int empty();
    
    int resourceCount();
    
    Resource[] getResources();
    
    <Query extends Resource>
    Query getResource(Class<Query> type);
    
    <Query extends Resource>
    Query[] getAllResources(Class<Query> type);
    
    <Query extends Object>
    Query lookFor(Class<? extends Query> type);
    
    Method lookForMethod(Class<?> returnType, String name, Class<?>... parameters);
    
}
