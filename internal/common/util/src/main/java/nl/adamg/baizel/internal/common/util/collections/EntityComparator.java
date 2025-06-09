package nl.adamg.baizel.internal.common.util.collections;


import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class EntityComparator {
    private static final Map<Class<?>, Comparator<?>> COMPARATORS = new ConcurrentHashMap<>();

    @SafeVarargs
    public static <T extends Comparable<T>> int compareBy(T left, T right, Function<T,? extends Comparable<?>>... fields) {
        return comparator(left.getClass(), fields).compare(left, right);
    }

    @SafeVarargs
    public static <T extends Comparable<T>> Comparator<T> comparator(Class<?> type, Function<T, ? extends Comparable<?>>... fields) {
        if (fields.length == 0) {
            return (a, b) -> 0;
        }
        @SuppressWarnings("unchecked")
        var comparator = (Comparator<T>)COMPARATORS.get(type);
        if(comparator == null) {
            @SuppressWarnings("unchecked")
            var nullsFirst = (Comparator<Comparable<?>>) Comparator.nullsFirst(Comparator.naturalOrder());
            comparator = Comparator.comparing(field(fields[0]), nullsFirst);
            for (var i = 1; i< fields.length; i++) {
                comparator = comparator.thenComparing(field(fields[i]), nullsFirst);
            }
            COMPARATORS.put(type, comparator);
        }
        return comparator;
    }

    private static <T extends Comparable<T>> Function<T, Comparable<Object>> field(Function<T, ? extends Comparable<?>> fields) {
        return t -> {
            @SuppressWarnings("unchecked")
            var cast = (Comparable<Object>) fields.apply(t);
            return cast;
        };
    }

}
