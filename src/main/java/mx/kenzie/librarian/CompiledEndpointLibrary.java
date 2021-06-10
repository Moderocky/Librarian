package mx.kenzie.librarian;

import mx.kenzie.librarian.error.EndpointLoaderException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

/**
 * A library that builds and class-loads a bytecode implementation of the query
 * interface in order to speed up execution.
 *
 * This is significantly faster than the typical proxy implementation and has
 * all the advantages of JIT, but is unable to use private and local methods or interfaces.
 *
 * @author Moderocky
 */
public class CompiledEndpointLibrary extends SimpleLibrary {
    
    protected volatile int compile;
    protected RuntimeClassLoader loader = new RuntimeClassLoader();
    protected final Map<Class<?>, Class<?>> endpoints = new HashMap<>();
    
    @Override
    public <Query> Query lookFor(Class<? extends Query> type) {
        assert type.isInterface();
        return buildClass(type);
    }
    
    protected synchronized <Query> Query buildClass(Class<? extends Query> type) {
        assert type.isInterface();
        final Object[] resources = this.getResources();
        final Method[] methods = type.getDeclaredMethods();
        final Object[] targets = new Object[methods.length];
        if (endpoints.containsKey(type)) {
            int current = 0;
            for (final Method method : methods) {
                for (final Object resource : resources) {
                    final Method match = AccessUtility.getMatchingMethod(resource, method);
                    if (match == null) continue;
                    targets[current] = resource;
                    current++;
                    break;
                }
            }
            return (Query) buildEndpoint(endpoints.get(type), targets);
        }
        final String namespace = "mx.kenzie.librarian.generated.$QueryClass" + this.hashCode() + "$" + (++compile);
        final String internalName = namespace.replace(".", "/");
        final ClassWriter writer = new ClassWriter(ASM9 + ClassWriter.COMPUTE_MAXS);
        writer.visit(V11, ACC_PUBLIC,
            internalName, null, "java/lang/Object",
            new String[]{this.getInternalName(type)});
        final FieldVisitor fieldVisitor;
        final MethodVisitor methodVisitor;
        fieldVisitor = writer.visitField(ACC_PRIVATE | ACC_FINAL, "targets", "[Ljava/lang/Object;", null, null);
        fieldVisitor.visitEnd();
        methodVisitor = writer.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/Object;)V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitFieldInsn(PUTFIELD, internalName, "targets", "[Ljava/lang/Object;");
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(2, 2);
        methodVisitor.visitEnd();
        int current = 0;
        for (final Method method : methods) {
            for (final Object resource : resources) {
                final Method match = AccessUtility.getMatchingMethod(resource, method);
                if (match == null) continue;
                targets[current] = resource;
                final GeneratorAdapter adapter = new GeneratorAdapter(
                    method.getModifiers() & ~ACC_ABSTRACT,
                    new org.objectweb.asm.commons.Method(method.getName(), this.getDescriptor(method)),
                    null, null, writer
                );
                this.writeCallerLink(adapter, internalName, match, current);
                current++;
                break;
            }
        }
        writer.visitEnd();
        final byte[] bytes = writer.toByteArray();
        final Class<? extends Query> compiled = (Class<? extends Query>) loader.loadClass(namespace, bytes);
        endpoints.put(type, compiled);
        return buildEndpoint(compiled, targets);
    }
    
    protected <Query> Query buildEndpoint(Class<Query> compiled, Object[] targets) {
        try {
            final Constructor<Query> constructor = compiled.getConstructor(Object[].class);
            AccessUtility.access(constructor);
            return constructor.newInstance((Object) targets);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            throw new EndpointLoaderException("Unable to create compiled endpoint for resource.", ex);
        }
    }
    
    protected void writeCallerLink(GeneratorAdapter methodVisitor, String internalName, Method method, int current) {
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, internalName, "targets", "[Ljava/lang/Object;");
        methodVisitor.visitIntInsn(BIPUSH, current);
        methodVisitor.visitInsn(AALOAD);
        methodVisitor.visitTypeInsn(CHECKCAST, this.getInternalName(method.getDeclaringClass()));
        this.loadArguments(methodVisitor, method);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, this.getInternalName(method.getDeclaringClass()), method.getName(), this.getDescriptor(method), false);
        methodVisitor.visitInsn(IRETURN);
        methodVisitor.visitMaxs(method.getParameterTypes().length, 2);
        methodVisitor.visitEnd();
    }
    
    @Override
    public int empty() {
        loader = new RuntimeClassLoader();
        endpoints.clear();
        return super.empty();
    }
    
    //region Internal Utilities
    
    protected void loadArguments(GeneratorAdapter methodVisitor, Method method) {
        final Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < method.getParameterTypes().length; i++) {
            final Class<?> type = types[i];
            loadArgument(methodVisitor, type, i+1);
        }
    }
    
    protected void loadArgument(GeneratorAdapter methodVisitor, Class<?> type, int index) {
        if (type == int.class) methodVisitor.visitVarInsn(ILOAD, index);
        else if (type == byte.class) methodVisitor.visitVarInsn(ILOAD, index);
        else if (type == char.class) methodVisitor.visitVarInsn(ILOAD, index);
        else if (type == short.class) methodVisitor.visitVarInsn(ILOAD, index);
        else if (type == boolean.class) methodVisitor.visitVarInsn(ILOAD, index);
        else if (type == long.class) methodVisitor.visitVarInsn(LLOAD, index);
        else if (type == float.class) methodVisitor.visitVarInsn(FLOAD, index);
        else if (type == double.class) methodVisitor.visitVarInsn(DLOAD, index);
        else methodVisitor.visitVarInsn(ALOAD, index);
    }
    
    protected String getInternalName(final Class<?> cls) {
        assert !cls.isArray();
        if (cls.isHidden()) {
            String name = cls.getName();
            int index = name.indexOf('/');
            return name.substring(0, index).replace('.', '/')
                + "." + name.substring(index + 1);
        } else {
            return cls.getName().replace('.', '/');
        }
    }
    
    protected String getDescriptor(final Method method) {
        return getDescriptor(method.getReturnType(), method.getParameterTypes());
    }
    
    protected String getDescriptor(final Class<?> ret, final Class<?>... params) {
        final StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (Class<?> type : params) {
            builder.append(type.descriptorString());
        }
        builder
            .append(")")
            .append(ret.descriptorString());
        return builder.toString();
    }
    //endregion
    
    //region Class Loader
    protected Class<?> loadClass(final String name, final byte[] bytes) {
        return loader.loadClass(name, bytes);
    }
    
    static class RuntimeClassLoader extends ClassLoader {
        public Class<?> loadClass(String name, byte[] bytecode) {
            return defineClass(name, bytecode, 0, bytecode.length);
        }
    }
    //endregion
    
}
