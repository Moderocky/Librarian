package mx.kenzie.librarian;

import mx.kenzie.cobweb.Registry;
import mx.kenzie.librarian.error.RegistryAccessException;
import mx.kenzie.librarian.error.UnmatchedEndpointException;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A more complex library designed to wrap a Remote registry and provide
 * access to Remote resources from external JVMs and Java applications.
 *
 * @author Moderocky
 */
public class RemoteLibrary implements Library<Remote> {
    
    protected final Registry registry;
    
    //region Constructors
    public RemoteLibrary() {
        this.registry = Registry.acquireLocal();
    }
    
    public RemoteLibrary(int port) {
        this.registry = Registry.acquireLocal(port);
    }
    
    public RemoteLibrary(InetAddress host) {
        this.registry = Registry.getRemote(host);
    }
    
    public RemoteLibrary(InetAddress host, int port) {
        this.registry = Registry.getRemote(host, port);
    }
    //endregion
    
    @Override
    public boolean register(Remote object) {
        try {
            return registry.export(object.hashCode() + "", object) != null;
        } catch (Throwable ex) {
            throw new RegistryAccessException("Unable to export object to remote.", ex);
        }
    }
    
    @Override
    public boolean unregister(Remote object) {
        try {
            return registry.unbind(object.hashCode() + "");
        } catch (Throwable ex) {
            throw new RegistryAccessException("Unable to unbind remote object.", ex);
        }
    }
    
    @Override
    public boolean unregisterAll(Class<? extends Remote> type) {
        boolean changed = false;
        try {
            for (final String key : registry.getBindings()) {
                final Remote resource = registry.retrieve(key);
                if (type.isInstance(resource)) {
                    changed = true;
                    registry.unbind(key);
                }
            }
        } catch (RemoteException ex) {
            throw new RegistryAccessException("Unable to access remote bindings.", ex);
        }
        return changed;
    }
    
    @Override
    public boolean unregisterAll() {
        try {
            return registry.emptyBindings() > 0;
        } catch (RemoteException ex) {
            throw new RegistryAccessException("Unable to access remote bindings.", ex);
        }
    }
    
    @Override
    public int empty() {
        try {
            return registry.emptyBindings();
        } catch (RemoteException e) {
            throw new RegistryAccessException("Unable to empty bindings.", e);
        }
    }
    
    @Override
    public int resourceCount() {
        try {
            return registry.getBindings().length;
        } catch (RemoteException ex) {
            throw new RegistryAccessException("Unable to access remote bindings.", ex);
        }
    }
    
    @Override
    public Remote[] getResources() {
        final List<Remote> resources = new ArrayList<>();
        try {
            for (final String key : registry.getBindings()) {
                final Remote resource = registry.retrieve(key);
                resources.add(resource);
            }
        } catch (RemoteException ex) {
            throw new RegistryAccessException("Unable to access remote bindings.", ex);
        }
        return resources.toArray(new Remote[0]);
    }
    
    @Override
    public <Query extends Remote> Query getResource(Class<Query> type) {
        for (final Remote resource : getResources()) {
            if (type.isInstance(resource)) return (Query) resource;
        }
        return null;
    }
    
    @Override
    public <Query extends Remote> Query[] getAllResources(Class<Query> type) {
        final List<Remote> objects = new ArrayList<>();
        for (final Remote resource : getResources()) {
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
            for (final Remote resource : this.getResources()) {
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
        for (final Remote resource : this.getResources()) {
            final Method match = AccessUtility.getMatchingMethod(resource, returnType, name, parameters);
            if (match == null) continue;
            AccessUtility.access(match);
            return match;
        }
        return null;
    }
}
