package nl.adamg.baizel.internal.common.util.collections;


import javax.annotation.CheckForNull;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class EntityComparator {
    private static final Map<Class<?>, Comparator<?>> COMPARATORS = new ConcurrentHashMap<>();

    @SafeVarargs
    public static <T> boolean equals(@CheckForNull T a, @CheckForNull Object b, Function<T, ?>... fields) {
        return equals(a, b, Arrays.asList(fields));
    }

    public static <T> boolean equals(@CheckForNull T a, @CheckForNull Object b, List<Function<T, ?>> fields) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        if (a.getClass() != b.getClass()) {
            return false;
        }
        @SuppressWarnings("unchecked")
        var bCast = (T)b;
        for (var field : fields) {
            if (!Objects.equals(field.apply(a), field.apply(bCast))) {
                return false;
            }
        }
        return true;
    }

    @SafeVarargs
    public static <T> int compareBy(T left, T right, Function<T,?>... fields) {
        return compareBy(left, right, Arrays.asList(fields));
    }

    public static <T> int compareBy(T left, T right, List<Function<T,?>> fields) {
        return comparator(left.getClass(), fields).compare(left, right);
    }

    public static <T> Comparator<T> comparator(Class<?> type, List<Function<T, ?>> fields) {
        if (fields.isEmpty()) {
            return (a, b) -> 0;
        }
        @SuppressWarnings("unchecked")
        var comparator = (Comparator<T>)COMPARATORS.get(type);
        if(comparator == null) {
            @SuppressWarnings("unchecked")
            var nullsFirst = (Comparator<Comparable<?>>) Comparator.nullsFirst(Comparator.naturalOrder());
            comparator = Comparator.comparing(castField(fields.get(0)), nullsFirst);
            for (var i = 1; i< fields.size(); i++) {
                comparator = comparator.thenComparing(castField(fields.get(i)), nullsFirst);
            }
            COMPARATORS.put(type, comparator);
        }
        return comparator;
    }

    @SafeVarargs
    public static <T> int hashCode(T that, Function<T, ?>... fields) {
        return hashCode(that, Arrays.asList(fields));
    }

    public static <T> int hashCode(T that, List<Function<T, ?>> fields) {
        var values = new Object[fields.size()];
        for (var i = 0; i < fields.size(); i++) {
            values[i] = fields.get(i).apply(that);
        }
        return Arrays.hashCode(values);
    }

    private static <T> Function<T, Comparable<Object>> castField(Function<T, ?> fields) {
        return t -> {
            @SuppressWarnings("unchecked")
            var cast = (Comparable<Object>) fields.apply(t);
            return cast;
        };
    }
}
