package nl.adamg.baizel.internal.bootstrap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class TypesUtil {
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

    private TypesUtil() {
    }
}
