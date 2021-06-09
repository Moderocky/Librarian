package mx.kenzie.librarian;

import sun.misc.Unsafe;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;

class AccessUtility {
    
    static Unsafe UNSAFE;
    private static Field methodAccessorField;
    private static Field rootField;
    
    static {
        try {
            UNSAFE = AccessController.doPrivileged((PrivilegedExceptionAction<Unsafe>) () -> {
                final Field field = Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                return (Unsafe) field.get(null);
            });
            final Field field = Class.class.getDeclaredField("module");
            final long offset = UNSAFE.objectFieldOffset(field);
            UNSAFE.putObject(AccessUtility.class, offset, Object.class.getModule());
            methodAccessorField = Method.class.getDeclaredField("methodAccessor");
            access(methodAccessorField);
            rootField = Method.class.getDeclaredField("root");
            access(rootField);
        } catch (Throwable ex) {
            UNSAFE = null;
        }
    }
    
    static Method getMatchingMethod(Object object, Method method) {
        Method match = null;
        for (Method m : object.getClass().getDeclaredMethods()) {
            if (m.getName().equals(method.getName())
                && Arrays.equals(m.getParameterTypes(), method.getParameterTypes())
                && method.getReturnType().isAssignableFrom(m.getReturnType())
            ) return m;
        }
        if (object.getClass().getSuperclass() == Object.class) return match;
        return getMatchingMethod(object.getClass().getSuperclass(), method);
    }
    
    static Method getMatchingMethod(Object object, Class<?> type, String name, Class<?>... params) {
        Method match = null;
        for (Method m : object.getClass().getDeclaredMethods()) {
            if (m.getName().equals(name)
                && Arrays.equals(m.getParameterTypes(), params)
                && type.isAssignableFrom(m.getReturnType())
            ) return m;
        }
        if (object.getClass().getSuperclass() == Object.class) return match;
        return getMatchingMethod(object.getClass().getSuperclass(), type, name, params);
    }
    
    static void squashAccessors(Method first, Method second) {
        if (UNSAFE == null) return;
        try {
            methodAccessorField.set(root(first), methodAccessorField.get(root(second)));
            methodAccessorField.set(first, methodAccessorField.get(root(second)));
        } catch (Throwable ignore) {
            ignore.printStackTrace();
        }
    }
    
    static Method root(Method method) {
        try {
            return (Method) rootField.get(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    static void access(final AccessibleObject object) {
        object.setAccessible(true);
    }
    
}
