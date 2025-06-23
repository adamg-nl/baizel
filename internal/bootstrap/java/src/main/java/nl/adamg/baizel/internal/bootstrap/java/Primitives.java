package nl.adamg.baizel.internal.bootstrap.java;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Primitives {
    /** lazily populated, non-exhaustive, includes non-boxing-non-primitive types ("normal types") mapped to nulls */
    private static final Map<Class<?>, Class<?>> BOXED_AND_PRIMITIVE_TYPES = new ConcurrentHashMap<>();
    /** lazily populated, non-exhaustive */
    private static final Map<Class<?>, Object> DEFAULT_PRIMITIVES = new ConcurrentHashMap<>();

    /**
     * @return null if type is not primitive
     */
    public static <T> T defaultValue(Class<? extends T> type) {
        var missingCacheMarker = (Integer)(-1); // this is not a default value, so it can't normally appear in this map
        var noDefaultMarker = (Integer)(-2); // this is not a default value, so it can't normally appear in this map
        var cached = DEFAULT_PRIMITIVES.getOrDefault(type, missingCacheMarker);
        if (noDefaultMarker.equals(cached)) {
            return null;
        }
        if (missingCacheMarker.equals(cached)) {
            cached = Array.get(Array.newInstance(unbox(type), 1), 0);
            if (cached == null) {
                cached = noDefaultMarker;
            }
            DEFAULT_PRIMITIVES.put(type, cached);
        }
        @SuppressWarnings("unchecked")
        var cast = (T) cached;
        return cached == noDefaultMarker ? null : cast;
    }

    public static boolean isDefaultOrEmpty(Object value) {
        return value == null ||
                (value instanceof Iterable<?> i && ! i.iterator().hasNext()) ||
                (value instanceof Map<?,?> m && m.isEmpty()) ||
                (value.getClass().isArray() && Array.getLength(value) == 0) ||
                value.equals("") ||
                value.equals(defaultValue(value.getClass()));
    }

    public static Class<?> unbox(Class<?> boxed) {
        if (boxed.isPrimitive()) {
            return boxed;
        }
        var missingCacheMarker = Void.class; // this is not a primitive type, so it can't normally appear in this map
        var cached = BOXED_AND_PRIMITIVE_TYPES.getOrDefault(boxed, missingCacheMarker);
        if (missingCacheMarker.equals(cached)) {
            try {
                cached = boxed; // by default assume nothing to unbox
                // boxed types all have public static TYPE field (allowed for reflection)
                var field = boxed.getField("TYPE");
                if (Class.class.equals(field.getType())) {
                    var unboxed = (Class<?>) field.get(null);
                    if (unboxed != null && boxed.isInstance(defaultValue(unboxed))) {
                        // we confirmed that the default value of primitive, after autoboxing, has our boxed type
                        cached = unboxed;
                    }
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
            BOXED_AND_PRIMITIVE_TYPES.put(boxed, cached);
        }
        return cached;
    }

    public static boolean isPrimitiveOrBoxed(Class<?> type) {
        return unbox(type).isPrimitive();
    }


    protected Primitives() {}
}
