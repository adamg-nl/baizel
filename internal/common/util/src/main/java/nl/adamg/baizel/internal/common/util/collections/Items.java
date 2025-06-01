package nl.adamg.baizel.internal.common.util.collections;

import nl.adamg.baizel.internal.common.util.functions.ThrowingFunction;
import nl.adamg.baizel.internal.common.util.functions.ThrowingPredicate;
import nl.adamg.baizel.internal.common.util.java.Reflection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

@SuppressWarnings("unused")
public class Items {
    public static <C extends Collection<O>, I, O, E extends Exception> C map(Collection<I> input, ThrowingFunction<I,O,E> mapping, C output) throws E {
        for(var i : input) {
            output.add(mapping.apply(i));
        }
        return output;
    }

    public static <I, O, E extends Exception> List<O> mapToList(I[] input, ThrowingFunction<I,O,E> mapping) throws E {
        return mapToList(Arrays.asList(input), mapping);
    }

    public static <I, O, E extends Exception> List<O> mapToList(Collection<I> input, ThrowingFunction<I,O,E> mapping) throws E {
        return map(input, mapping, new ArrayList<>());
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

    /** @return read-only array */
    public static <I, O, E extends Exception> O[] mapToArray(Collection<I> input, ThrowingFunction<I,O,E> mapping) throws E {
        var output = mapToList(input, mapping);
        var type = Reflection.findCommonSuperClass(output);
        @SuppressWarnings("unchecked") // safe for read-only use
        var array = (O[]) Array.newInstance(type, output.size());
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
}
