package nl.adamg.baizel.internal.bootstrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

class Collections {
    interface ThrowingFunction<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    static <I, O, E extends Exception> List<O> mapToList(Collection<I> input, ThrowingFunction<I, O, E> mapping) throws E {
        var output = new ArrayList<O>(input.size());
        for (var i : input) {
            output.add(mapping.apply(i));
        }
        return output;
    }

    static <T extends Comparable<T>> Set<T> mergeSet(Collection<? extends Collection<T>> inputs) {
        var set = new TreeSet<T>();
        for (var input : inputs) {
            set.addAll(input);
        }
        return set;
    }
}
