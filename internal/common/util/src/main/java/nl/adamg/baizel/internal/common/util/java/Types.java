package nl.adamg.baizel.internal.common.util.java;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Types {
    /** lazily populated, non-exhaustive */
    private static final Map<Class<?>, Object> DEFAULT_OBJECTS = new ConcurrentHashMap<>();

    public static Class<?> findCommonSuperClass(Collection<?> objects) {
        if(objects.isEmpty()) {
            return Object.class;
        }
        var superclass = objects.iterator().next().getClass();
        for(var object : objects) {
            while (! superclass.isAssignableFrom(object.getClass()) && ! superclass.equals(Object.class)) {
                superclass = superclass.getSuperclass();
            }
        }
        return superclass;
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

    private Types() {}
}
