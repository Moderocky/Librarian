package mx.kenzie.librarian;

import mx.kenzie.librarian.error.UnmatchedEndpointException;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple library implementation that stores resources in a list.
 * Also provides a basic query system.
 *
 * @author Moderocky
 */
public class SimpleLibrary implements Library<Object> {
    
    protected final List<Object> resources = new ArrayList<>();
    
    @Override
    public boolean register(Object object) {
        return resources.add(object);
    }
    
    @Override
    public boolean unregister(Object object) {
        return resources.remove(object);
    }
    
    @Override
    public boolean unregisterAll(Class<? extends Object> type) {
        boolean changed = false;
        for (final Object resource : new ArrayList<>(resources)) {
            if (type.isInstance(resource)) {
                changed = true;
                resources.remove(resource);
            }
        }
        return changed;
    }
    
    @Override
    public boolean unregisterAll() {
        final boolean changed = resources.size() > 0;
        resources.clear();
        return changed;
    }
    
    @Override
    public int empty() {
        final int size = resources.size();
        resources.clear();
        return size;
    }
    
    @Override
    public int resourceCount() {
        return resources.size();
    }
    
    @Override
    public Object[] getResources() {
        return resources.toArray(new Object[0]);
    }
    
    @Override
    public <Query> Query getResource(Class<Query> type) {
        for (final Object resource : resources) {
            if (type.isInstance(resource)) return (Query) resource;
        }
        return null;
    }
    
    @Override
    public <Query> Query[] getAllResources(Class<Query> type) {
        final List<Object> objects = new ArrayList<>();
        for (final Object resource : resources) {
            if (type.isInstance(resource)) objects.add(resource);
        }
        return objects.toArray((Query[]) Array.newInstance(type, 0));
    }
    
    @Override
    public <Query> Query lookFor(Class<? extends Query> type) {
        class Handler implements InvocationHandler {
            final Map<Method, Endpoint> map = new HashMap<>();
            
            @Override
            public Object invoke(Object proxy, Method method, Object... args) {
                final Endpoint endpoint = map.get(method);
                if (endpoint == null)
                    throw new UnmatchedEndpointException("Unmatched library method: '" + method.getName() + "'");
                return endpoint.invoke(args);
            }
        }
        final Handler handler = new Handler();
        for (final Method method : type.getDeclaredMethods()) {
            for (final Object resource : this.getResources()) {
                final Method match = AccessUtility.getMatchingMethod(resource, method);
                if (match == null) continue;
                AccessUtility.access(match);
                handler.map.put(method, new Endpoint(resource, match));
                break;
            }
        }
        return (Query) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{type}, handler);
    }
    
    @Override
    public Method lookForMethod(Class<?> returnType, String name, Class<?>... parameters) {
        for (final Object resource : this.getResources()) {
            final Method match = AccessUtility.getMatchingMethod(resource, returnType, name, parameters);
            if (match == null) continue;
            AccessUtility.access(match);
            return match;
        }
        return null;
    }
}
