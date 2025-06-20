package nl.adamg.baizel.internal.common.util.functions;

import javax.annotation.CheckForNull;

@SuppressWarnings("unused")
@FunctionalInterface
public interface Predicate<T, E extends Exception> {
    boolean test(T t) throws E;

    @FunctionalInterface
    interface Nullable<T, E extends Exception> {
        boolean test(@CheckForNull T t) throws E;
    }
}
