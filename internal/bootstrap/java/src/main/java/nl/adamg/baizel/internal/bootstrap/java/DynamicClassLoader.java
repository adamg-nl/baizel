package nl.adamg.baizel.internal.bootstrap.java;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Utility for performing operations on objects of types unknown at build time.
 */
public final class DynamicClassLoader<T extends ClassLoader & Closeable> implements AutoCloseable {
    private final T classLoader;

    public static DynamicClassLoader<URLClassLoader> forPaths(List<Path> paths, Class<?> owner) {
        var urls = paths.stream().map(DynamicClassLoader::url).toArray(URL[]::new);
        var urlLoader = new URLClassLoader(urls, owner.getClassLoader());
        return new DynamicClassLoader<>(urlLoader);
    }

    public <T> T requireConstruct(String className, Object... args) {
        return Objects.requireNonNull(construct(className, args));
    }

    public <T> T requireGet(Object subject, String fieldName) {
        return Objects.requireNonNull(get(subject, fieldName));
    }

    public <T> T requireInvoke(Object subject, String methodName, Object... args) {
        return Objects.requireNonNull(invoke(subject, methodName, args));
    }

    /*@CheckForNull*/
    public <T> T construct(String className, Object... args) {
        try {
            for (var constructor : classLoader.loadClass(className).getConstructors()) {
                if (isParameterMatch(constructor.getParameterTypes(), args)) {
                    @SuppressWarnings("unchecked")
                    var result = (T) constructor.newInstance(args);
                    return result;
                }
            }
            return null;
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    /*@CheckForNull*/
    @SuppressWarnings("unchecked")
    public <T> T get(Object subject, String fieldName) {
        try {
            if (subject instanceof String className) {
                subject = classLoader.loadClass(className);
            }
        } catch (ReflectiveOperationException e) {
            return null;
        }
        if (subject instanceof Class<?> class_ && class_.isEnum()) {
            for (var e : class_.getEnumConstants()) {
                if (((Enum<?>) e).name().equals(fieldName)) {
                    return (T) e;
                }
            }
        }
        var exactType = (subject instanceof Class<?> clazz) ? clazz : subject.getClass();
        for (var type : TypesUtil.inheritanceChain(exactType)) {
            try {
                for (var field : type.getFields()) {
                    if (!field.getName().equals(fieldName)) {
                        continue;
                    }
                    if ((exactType == subject) != Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    return (T) field.get((exactType == subject) ? null : subject);
                }
            } catch (ReflectiveOperationException ignored) {
                throw new RuntimeException(ignored);
            }
        }
        return null;
    }

    /**
     * @param subject object, or class, or class name
     */
    /*@CheckForNull*/
    public <T> T invoke(Object subject, String methodName, Object... args) {
        try {
            if (subject instanceof String className) {
                subject = classLoader.loadClass(className);
            }
        } catch (ReflectiveOperationException e) {
            return null;
        }
        var exactType = (subject instanceof Class<?> clazz) ? clazz : subject.getClass();
        for (var type : TypesUtil.inheritanceChain(exactType)) {
            try {
                for (var method : type.getMethods()) {
                    if (!method.getName().equals(methodName)) {
                        continue;
                    }
                    if ((exactType == subject) != Modifier.isStatic(method.getModifiers())) {
                        continue;
                    }
                    if (isParameterMatch(method.getParameterTypes(), args)) {
                        @SuppressWarnings("unchecked")
                        var result = (T) method.invoke((exactType == subject) ? null : subject, args);
                        return result;
                    }
                }
            } catch (ReflectiveOperationException ignored) {
                throw new RuntimeException(ignored);
            }
        }
        return null;
    }

    public void set(Object subject, String fieldName, /*@CheckForNull*/ Object value) {
        try {
            if (subject instanceof String className) {
                subject = classLoader.loadClass(className);
            }
        } catch (ReflectiveOperationException e) {
            return;
        }
        var exactType = (subject instanceof Class<?> clazz) ? clazz : subject.getClass();
        for (var type : TypesUtil.inheritanceChain(exactType)) {
            try {
                for (var field : type.getFields()) {
                    if (!field.getName().equals(fieldName)) {
                        continue;
                    }
                    if ((exactType == subject) != Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    if (TypesUtil.isAssignable(field.getType(), value)) {
                        field.set((exactType == subject) ? null : subject, value);
                    }
                }
            } catch (ReflectiveOperationException ignored) {
                throw new RuntimeException(ignored);
            }
        }
    }

    /**
     * Replacement for {@code Class.forName(name)}.
     */
    public Class<?> forName(String name) {
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        classLoader.close();
    }

    // region private utils
    private boolean isParameterMatch(Class<?>[] paramTypes, Object[] args) {
        if (paramTypes.length != args.length) {
            return false;
        }
        for (var i = 0; i < args.length; i++) {
            if (!TypesUtil.isAssignable(paramTypes[i], args[i])) {
                return false;
            }
        }
        return true;
    }

    private static URL url(Path p) {
        try {
            return p.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    // endregion

    //region generated code
    public DynamicClassLoader(T classLoader) {
        this.classLoader = classLoader;
    }
    //endregion
}
