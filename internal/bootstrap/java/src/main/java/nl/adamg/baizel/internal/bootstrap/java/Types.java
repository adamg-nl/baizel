package nl.adamg.baizel.internal.bootstrap.java;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Types {
    /** lazily populated, non-exhaustive */
    private static final Map<Class<?>, Object> DEFAULT_OBJECTS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, Class<?>> BOXED_TYPES = new ConcurrentHashMap<>();

    public static List<Class<?>> inheritanceChain(Class<?> descendant) {
        var chain = new ArrayList<Class<?>>();
        while (descendant != null) {
            for (var parentInterface : descendant.getInterfaces()) {
                chain.addAll(inheritanceChain(parentInterface));
            }
            chain.add(descendant);
            descendant = descendant.getSuperclass();
        }
        return chain;
    }

    /**
     * Standard Class.isAssignableFrom returns false for pairs of primitive type and boxed
     * equivalent. In practice these types are assignable because of auto (un)boxing. This method
     * returns true for them.
     */
    public static boolean isAssignableFrom(Class<?> a, Class<?> b) {
        return getBoxedType(a).isAssignableFrom(getBoxedType(b));
    }

    public static Class<?> getBoxedType(Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        var cached = BOXED_TYPES.get(type);
        if (cached != null) {
            return cached;
        }
        var boxed = Array.get(Array.newInstance(type, 1), 0).getClass();
        BOXED_TYPES.put(type, boxed);
        return boxed;
    }

    public static boolean isAssignable(Class<?> type, /*@CheckForNull*/ Object value) {
        if (value == null) {
            return !type.isPrimitive();
        }
        return isAssignableFrom(type, value.getClass());
    }

    public static <T> T defaultValue(Class<? extends T> type) {
        var missingCacheMarker = (Integer)(-1); // this is not a default object, so it can't normally appear in this map
        var noDefaultMarker = (Integer)(-2); // this is not a default value, so it can't normally appear in this map
        var cached = DEFAULT_OBJECTS.getOrDefault(type, missingCacheMarker);
        if (noDefaultMarker.equals(cached)) {
            return null;
        }
        if (missingCacheMarker.equals(cached)) {
            cached = null; // by default assume no default value
            var primitiveValue = Primitives.defaultValue(type);
            if (primitiveValue != null) {
                cached = primitiveValue;
            } else {
                try {
                    cached = type.getConstructor().newInstance();
                } catch (ReflectiveOperationException ignored) {
                    cached = noDefaultMarker;
                }
            }
            DEFAULT_OBJECTS.put(type, cached);
        }
        return cached == noDefaultMarker ? null : type.cast(cached);
    }

    protected Types() {}
}
