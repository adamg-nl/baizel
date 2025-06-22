package nl.adamg.baizel.internal.bootstrap.util.collections;

import java.util.Arrays;
import nl.adamg.baizel.internal.bootstrap.util.functions.Function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import nl.adamg.baizel.internal.bootstrap.util.functions.Predicate;

/**
 * Bootstrap part of {@code nl.adamg.baizel.internal.common.util.collections.Items}
 */
public class Items {
    public static <I, O, E extends Exception> List<O> mapToList(Collection<I> input, Function<I, O, E> mapping) throws E {
        var output = new ArrayList<O>(input.size());
        for (var i : input) {
            output.add(mapping.apply(i));
        }
        return output;
    }

    public static <T extends Comparable<T>> Set<T> mergeSet(Collection<? extends Collection<T>> inputs) {
        var set = new TreeSet<T>();
        for (var input : inputs) {
            set.addAll(input);
        }
        return set;
    }

    public static <T, E extends Exception> boolean allMatch(T[] input, Predicate<T, E> predicate) throws E {
        return allMatch(Arrays.asList(input), predicate);
    }

    public static <T, E extends Exception> boolean allMatch(Collection<T> input, Predicate<T, E> predicate) throws E {
        for(var i : input) {
            if (! predicate.test(i)) {
                return false;
            }
        }
        return true;
    }
}
