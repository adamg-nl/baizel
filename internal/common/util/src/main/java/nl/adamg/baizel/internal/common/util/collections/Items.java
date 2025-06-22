package nl.adamg.baizel.internal.common.util.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import nl.adamg.baizel.internal.common.util.Exceptions;
import nl.adamg.baizel.internal.common.util.functions.Function;
import nl.adamg.baizel.internal.common.util.functions.Predicate;
import nl.adamg.baizel.internal.common.util.java.typeref.TypeRef2;

import java.lang.reflect.Array;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
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

    public static <C extends Collection<O>, I, O, E extends Exception> C map(Collection<I> input, Function<I,O,E> mapping, C output) throws E {
        for(var i : input) {
            output.add(mapping.apply(i));
        }
        return output;
    }

    public static <I, O, E extends Exception> List<O> mapToList(I[] input, Function<I,O,E> mapping) throws E {
        return mapToList(Arrays.asList(input), mapping);
    }

    public static <I, O, E extends Exception> Set<O> mapToSet(I[] input, Function<I,O,E> mapping) throws E {
        return mapToSet(Arrays.asList(input), mapping);
    }

    public static <I, O, E extends Exception> Set<O> mapToSet(Collection<I> input, Function<I,O,E> mapping) throws E {
        return map(input, mapping, new HashSet<>());
    }

    public static <I, O extends Comparable<O>, E extends Exception> SortedSet<O> mapToSortedSet(I[] input, Function<I,O,E> mapping) throws E {
        return mapToSortedSet(Arrays.asList(input), mapping);
    }

    public static <I, O extends Comparable<O>, E extends Exception> SortedSet<O> mapToSortedSet(Collection<I> input, Function<I,O,E> mapping) throws E {
        return map(input, mapping, new TreeSet<>());
    }

    public static <I, O, E extends Exception> O[] mapToArray(I[] input, Function<I,O,E> mapping) throws E {
        return mapToArray(Arrays.asList(input), mapping);
    }

    /**
     * @return read-only array
     * not safe for writing, because it might have been dynamically constructed with more specific runtime type than declared one
     */
    public static <I, O, E extends Exception> O[] mapToArray(Collection<I> input, Function<I,O,E> mapping) throws E {
        var output = mapToList(input, mapping);
        if (output.isEmpty()) {
            @SuppressWarnings("unchecked") // empty array is immutable by nature, so safe
            var emptyArray = (O[]) new Object[0];
            return emptyArray;
        }
        var superclass = output.get(0).getClass();
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

    public static <I, O, E extends Exception> O[] mapToArray(I[] input, Function<I,O,E> mapping, Function<Integer, O[], E> outputFactory) throws E {
        return mapToArray(Arrays.asList(input), mapping, outputFactory);
    }

    public static <I, O, E extends Exception> O[] mapToArray(Collection<I> input, Function<I,O,E> mapping, Function<Integer, O[], E> outputFactory) throws E {
        var array = outputFactory.apply(input.size());
        var i = 0;
        for(var item : input) {
            array[i] = mapping.apply(item);
            i++;
        }
        return array;
    }

    public static <T, E extends Exception> boolean anyMatch(T[] input, Predicate<T, E> predicate) throws E {
        return anyMatch(Arrays.asList(input), predicate);
    }

    public static <T, E extends Exception> boolean anyMatch(Collection<T> input, Predicate<T, E> predicate) throws E {
        for(var i : input) {
            if (predicate.test(i)) {
                return true;
            }
        }
        return false;
    }

    public static <T, E extends Exception> boolean noneMatch(T[] input, Predicate<T, E> predicate) throws E {
        return noneMatch(Arrays.asList(input), predicate);
    }

    public static <T, E extends Exception> boolean noneMatch(Collection<T> input, Predicate<T, E> predicate) throws E {
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

    public static <K, V> Map<K, List<V>> invertMapOfLists(Map<V, List<K>> input) {
        @SuppressWarnings("unchecked")
        var inverted = Items.newOfType((Map<K, List<V>>)(Object)input);
        for (var entry : input.entrySet()) {
            for (var value : entry.getValue()) {
                inverted.computeIfAbsent(value, k -> new ArrayList<>()).add(entry.getKey());
            }
        }
        return inverted;
    }

    public static <T, E extends Exception> String toString(Iterable<T> input, String separator, Function<T,String, E> toString) throws E {
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

    public static <I, K2 extends Comparable<K2>, V2, E extends Exception> Map<K2, V2> mapToSortedMap(Iterable<I> input, Function<I, K2, E> keySelector, Function<I, V2, E> valueSelector) throws E {
        return mapToMap(input, keySelector, valueSelector, new TreeMap<>());
    }

    public static <I, K2, V2, E extends Exception> Map<K2, V2> mapToMap(Iterable<I> input, Function<I, K2, E> keySelector, Function<I, V2, E> valueSelector) throws E {
        return mapToMap(input, keySelector, valueSelector, new HashMap<>());
    }

    public static <I, K2, V2, E extends Exception> Map<K2, V2> mapToMap(Iterable<I> input, Function<I, K2, E> keySelector, Function<I, V2, E> valueSelector, Map<K2, V2> output) throws E {
        output.clear();
        for(var i : input) {
            output.put(keySelector.apply(i), valueSelector.apply(i));
        }
        return output;
    }

    public static <T> Set<T> newConcurrentSet() {
        return Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    /// Wrapper for [Map#computeIfAbsent] that supports checked exceptions.
    public static <K, V, E extends Exception> V computeIfAbsent(Map<K, V> map, K key, Function<K, V, E> mapping, Class<E> exceptionType) throws E {
        var exception = new AtomicReference<Exception>();
        var value = map.computeIfAbsent(key, k -> {
            try {
                return mapping.apply(k);
            } catch (Exception e) {
                exception.set(e);
                return null;
            }
        });
        Exceptions.rethrowIfAny(exception.get(), exceptionType);
        return Objects.requireNonNull(value);
    }


    @SafeVarargs
    public static <T, K extends T, V extends T> Map<K, V> map(Map<K, V> output, TypeRef2<K, V> typeRef, T... keyValuePairs) {
        for(var i=0; i<keyValuePairs.length-1; i+=2) {
            var keyCast = typeRef.t1().cast(keyValuePairs[i]);
            var valueCast = typeRef.t2().cast(keyValuePairs[i+1]);
            output.put(keyCast, valueCast);
        }
        return output;
    }

    @SafeVarargs
    public static <T, K extends T, V extends T> Map<K, V> map(TypeRef2<K, V> typeRef, T... keyValuePairs) {
        return map(new HashMap<>(), typeRef, keyValuePairs);
    }

    public static <I> List<I> flattenList(Collection<List<I>> input) {
        if (input.isEmpty()) {
            return List.of();
        }
        var output = newOfType(input.iterator().next());
        for(var inner : input) {
            output.addAll(inner);
        }
        return output;
    }

    public static <I> Set<I> flattenSet(Collection<Set<I>> input) {
        if (input.isEmpty()) {
            return Set.of();
        }
        var output = newOfType(input.iterator().next());
        for(var inner : input) {
            output.addAll(inner);
        }
        return output;
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

    public static <T, E extends Exception> T[] filter(T[] input, Predicate<T, E> predicate) throws E {
        var result = filter(Arrays.asList(input), predicate);
        @SuppressWarnings("unchecked")
        var output = (T[]) Array.newInstance(input.getClass().getComponentType(), result.size());
        return result.toArray(output);
    }

    public static <L extends List<T>, T, E extends Exception> L filter(L input, Predicate<T, E> predicate) throws E {
        return filter(input, predicate, newOfType(input));
    }

    public static <L extends List<T>, T, E extends Exception> L filter(L input, Predicate<T, E> predicate, L output) throws E {
        for (var element : input) {
            if (predicate.test(element)) {
                output.add(element);
            }
        }
        return output;
    }

    public static <S extends Set<T>, T, E extends Exception> S filter(S input, Predicate<T, E> predicate) throws E {
        return filter(input, predicate, newOfType(input));
    }

    public static <S extends Set<T>, T, E extends Exception> S filter(S input, Predicate<T, E> predicate, S output) throws E {
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
