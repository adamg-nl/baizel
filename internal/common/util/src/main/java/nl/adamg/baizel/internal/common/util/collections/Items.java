package nl.adamg.baizel.internal.common.util.collections;

import nl.adamg.baizel.internal.bootstrap.util.functions.ThrowingFunction;
import nl.adamg.baizel.internal.common.util.functions.ThrowingPredicate;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Supplier;

/**
 * @see nl.adamg.baizel.internal.bootstrap.util.collections.Items
 */
@SuppressWarnings("unused")
public class Items extends nl.adamg.baizel.internal.bootstrap.util.collections.Items {
    private static final Map<Class<?>, Supplier<?>> COMMON_COLLECTION_CONSTRUCTORS = Map.of(
            ArrayList.class, ArrayList::new,
            LinkedList.class, LinkedList::new,
            HashSet.class, HashSet::new,
            LinkedHashSet.class, LinkedHashSet::new,
            TreeSet.class, TreeSet::new,
            HashMap.class, HashMap::new,
            LinkedHashMap.class, LinkedHashMap::new,
            TreeMap.class, TreeMap::new
    );
    private static final Map<Class<?>, Class<?>> COMMON_COLLECTION_TYPES = Map.of(
            List.class, ArrayList.class,
            Set.class, HashSet.class,
            Map.class, HashMap.class
    );

    public static <C extends Collection<O>, I, O, E extends Exception> C map(Collection<I> input, ThrowingFunction<I,O,E> mapping, C output) throws E {
        for(var i : input) {
            output.add(mapping.apply(i));
        }
        return output;
    }

    public static <I, O, E extends Exception> List<O> mapToList(I[] input, ThrowingFunction<I,O,E> mapping) throws E {
        return mapToList(Arrays.asList(input), mapping);
    }

    public static <I, O, E extends Exception> Set<O> mapToSet(I[] input, ThrowingFunction<I,O,E> mapping) throws E {
        return mapToSet(Arrays.asList(input), mapping);
    }

    public static <I, O, E extends Exception> Set<O> mapToSet(Collection<I> input, ThrowingFunction<I,O,E> mapping) throws E {
        return map(input, mapping, new HashSet<>());
    }

    public static <I, O extends Comparable<O>, E extends Exception> SortedSet<O> mapToSortedSet(I[] input, ThrowingFunction<I,O,E> mapping) throws E {
        return mapToSortedSet(Arrays.asList(input), mapping);
    }

    public static <I, O extends Comparable<O>, E extends Exception> SortedSet<O> mapToSortedSet(Collection<I> input, ThrowingFunction<I,O,E> mapping) throws E {
        return map(input, mapping, new TreeSet<>());
    }

    public static <I, O, E extends Exception> O[] mapToArray(I[] input, ThrowingFunction<I,O,E> mapping) throws E {
        return mapToArray(Arrays.asList(input), mapping);
    }

    /**
     * @return read-only array
     * not safe for writing, because it might have been dynamically constructed with more specific runtime type than declared one
     */
    public static <I, O, E extends Exception> O[] mapToArray(Collection<I> input, ThrowingFunction<I,O,E> mapping) throws E {
        var output = mapToList(input, mapping);
        if (output.isEmpty()) {
            @SuppressWarnings("unchecked") // empty array is immutable by nature, so safe
            var emptyArray = (O[]) new Object[0];
            return emptyArray;
        }
        var superclass = output.getFirst().getClass();
        for(var object : output) {
            while (! superclass.isAssignableFrom(object.getClass()) && ! superclass.equals(Object.class)) {
                superclass = superclass.getSuperclass();
            }
        }
        @SuppressWarnings("unchecked") // safe for read-only use
        var array = (O[]) Array.newInstance(superclass, output.size());
        for (var i = 0; i < output.size(); i++) {
            array[i] = output.get(i);
        }
        return array;
    }

    public static <I, O, E extends Exception> O[] mapToArray(I[] input, ThrowingFunction<I,O,E> mapping, ThrowingFunction<Integer, O[], E> outputFactory) throws E {
        return mapToArray(Arrays.asList(input), mapping, outputFactory);
    }

    public static <I, O, E extends Exception> O[] mapToArray(Collection<I> input, ThrowingFunction<I,O,E> mapping, ThrowingFunction<Integer, O[], E> outputFactory) throws E {
        var array = outputFactory.apply(input.size());
        var i = 0;
        for(var item : input) {
            array[i] = mapping.apply(item);
            i++;
        }
        return array;
    }

    public static <T, E extends Exception> boolean allMatch(T[] input, ThrowingPredicate<T, E> predicate) throws E {
        return allMatch(Arrays.asList(input), predicate);
    }

    public static <T, E extends Exception> boolean allMatch(Collection<T> input, ThrowingPredicate<T, E> predicate) throws E {
        for(var i : input) {
            if (! predicate.test(i)) {
                return false;
            }
        }
        return true;
    }

    public static <T, E extends Exception> boolean anyMatch(T[] input, ThrowingPredicate<T, E> predicate) throws E {
        return anyMatch(Arrays.asList(input), predicate);
    }

    public static <T, E extends Exception> boolean anyMatch(Collection<T> input, ThrowingPredicate<T, E> predicate) throws E {
        for(var i : input) {
            if (predicate.test(i)) {
                return true;
            }
        }
        return false;
    }

    public static <T, E extends Exception> boolean noneMatch(T[] input, ThrowingPredicate<T, E> predicate) throws E {
        return noneMatch(Arrays.asList(input), predicate);
    }

    public static <T, E extends Exception> boolean noneMatch(Collection<T> input, ThrowingPredicate<T, E> predicate) throws E {
        return ! anyMatch(input, predicate);
    }

    public static <K, V> Map<V, List<K>> invert(Map<K, V> map) {
        var result = new LinkedHashMap<V, List<K>>();
        for (var entry : map.entrySet()) {
            var value = entry.getValue();
            var key = entry.getKey();
            result.computeIfAbsent(value, k -> new ArrayList<>()).add(key);
        }
        return result;
    }

    public static <T, E extends Exception> String toString(List<T> input, String separator, ThrowingFunction<T,String, E> toString) throws E {
        var output = new StringBuilder();
        var first = true;
        for(var item : input) {
            if (! first) {
                output.append(separator);
            } else {
                first = false;
            }
            output.append(toString.apply(item));
        }
        return output.toString();
    }

    public <T> T first(List<T> input) {
        if (input.isEmpty()) {
            throw new NoSuchElementException();
        }
        return input.get(0);
    }

    public static <T> T last(List<T> input) {
        if (input.isEmpty()) {
            throw new NoSuchElementException();
        }
        return input.get(input.size()-1);
    }

    public static <T, E extends Exception> T[] filter(T[] input, ThrowingPredicate<T, E> predicate) throws E {
        var result = filter(Arrays.asList(input), predicate);
        @SuppressWarnings("unchecked")
        var output = (T[]) Array.newInstance(input.getClass().getComponentType(), result.size());
        return result.toArray(output);
    }

    public static <L extends List<T>, T, E extends Exception> L filter(L input, ThrowingPredicate<T, E> predicate) throws E {
        return filter(input, predicate, newOfType(input));
    }

    public static <L extends List<T>, T, E extends Exception> L filter(L input, ThrowingPredicate<T, E> predicate, L output) throws E {
        for (var element : input) {
            if (predicate.test(element)) {
                output.add(element);
            }
        }
        return output;
    }

    public static <S extends Set<T>, T, E extends Exception> S filter(S input, ThrowingPredicate<T, E> predicate) throws E {
        return filter(input, predicate, newOfType(input));
    }

    public static <S extends Set<T>, T, E extends Exception> S filter(S input, ThrowingPredicate<T, E> predicate, S output) throws E {
        for (var element : input) {
            if (predicate.test(element)) {
                output.add(element);
            }
        }
        return output;
    }

    /**
     * @return a new collection of the same type as input (e.g. TreeSet -> TreeSet)
     * Uses non-reflective O(1) constructor lookup for common collections, and reflective instantiation for others.
     */
    @SuppressWarnings("unchecked")
    public static <T> T newOfType(T input) {
        var knownConstructor = COMMON_COLLECTION_CONSTRUCTORS.get(input.getClass());
        if (knownConstructor != null) {
            return (T) input.getClass().cast(knownConstructor.get());
        }
        for(var commonCollectionType : COMMON_COLLECTION_TYPES.keySet()) {
            if (commonCollectionType.isInstance(input)) {
                return (T) COMMON_COLLECTION_CONSTRUCTORS.get(COMMON_COLLECTION_TYPES.get(commonCollectionType)).get();
            }
        }
        try {
            return (T) input.getClass().getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {

            throw new IllegalArgumentException("unsupported collection type: " + input.getClass().getCanonicalName());
        }
    }
}
